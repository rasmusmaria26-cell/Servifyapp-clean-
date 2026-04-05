package com.servify.app.feature.marketplace.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.marketplace.domain.BookingState
import com.servify.app.feature.marketplace.domain.Quote
import com.servify.app.feature.marketplace.domain.RequestStatus
import com.servify.app.feature.marketplace.domain.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

sealed interface ActionState {
    data object Idle : ActionState
    data object Loading : ActionState
    data class Error(val message: String) : ActionState
}

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _requestId = MutableStateFlow<String?>(null)
    
    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    val bookingState: StateFlow<BookingState> = _requestId
        .filterNotNull()
        .flatMapLatest { id ->
            // Combine both sources of truth from Supabase
            combine(
                marketplaceRepository.observeActiveRequest(id),
                marketplaceRepository.observeQuotesForRequest(id)
            ) { request, quotes -> request to quotes }
        }
        .flatMapLatest { (request, quotes) ->
            if (request.status == RequestStatus.OPEN) {
                // If the request is OPEN, spin up a local ticker flow that emits every second.
                // This continuously evaluates the remaining time client-side without pinging the DB.
                flow {
                    while (currentCoroutineContext().isActive) {
                        val remaining = request.quoteDeadline.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()
                        
                        if (remaining <= 0) {
                            emit(BookingState.Expired)
                            break // Stop ticking once expired client-side
                        } else {
                            emit(BookingState.ActiveBidding(request, quotes, formatTime(remaining)))
                        }
                        
                        delay(1000L)
                    }
                }
            } else {
                // If it's NOT OPEN, the ticker is entirely bypassed (wasting zero CPU).
                when (request.status) {
                    RequestStatus.AWAITING_PAYMENT -> {
                        val acceptedQuote = quotes.find { it.id == request.acceptedQuoteId }
                        if (acceptedQuote != null) {
                            flowOf(BookingState.AwaitingPayment(request, acceptedQuote))
                        } else {
                            flowOf(BookingState.Error("Accepted quote not found on server state."))
                        }
                    }
                    RequestStatus.EXPIRED -> flowOf(BookingState.Expired)
                    RequestStatus.ASSIGNED, RequestStatus.COMPLETED -> flowOf(BookingState.Completed(request))
                    else -> flowOf(BookingState.Error("Unexpected status: \${request.status}"))
                }
            }
        }
        .catch { e -> emit(BookingState.Error(e.message ?: "Connection error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BookingState.Loading
        )

    fun loadBiddingSession(requestId: String) {
        _requestId.value = requestId
    }

    fun acceptQuote(quote: Quote) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            marketplaceRepository.acceptQuote(quote.id)
                .onSuccess { _actionState.value = ActionState.Idle }
                .onFailure { _actionState.value = ActionState.Error(it.message ?: "Failed to accept quote") }
        }
    }

    fun cancelPayment(requestId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            marketplaceRepository.cancelPayment(requestId)
                .onSuccess { _actionState.value = ActionState.Idle }
                .onFailure { _actionState.value = ActionState.Error(it.message ?: "Failed to cancel payment") }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        // Use standard Kotlin String.format or padStart
        return "\${minutes.toString().padStart(2, '0')}:\${seconds.toString().padStart(2, '0')}"
    }
}
