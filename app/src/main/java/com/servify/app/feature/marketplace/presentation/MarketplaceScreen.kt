package com.servify.app.feature.marketplace.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.servify.app.feature.marketplace.domain.BookingState

import com.servify.app.feature.marketplace.presentation.MarketplaceViewModel

@Composable
fun MarketplaceScreen(
    requestId: String,
    snackbarHostState: SnackbarHostState,
    viewModel: MarketplaceViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // Load the specific request
    LaunchedEffect(requestId) {
        viewModel.loadBiddingSession(requestId)
    }

    val bookingState by viewModel.bookingState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    // Handle Action Errors globally
    LaunchedEffect(actionState) {
        if (actionState is ActionState.Error) {
            snackbarHostState.showSnackbar((actionState as ActionState.Error).message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(targetState = bookingState, label = "BookingStateCrossfade") { state ->
            when (state) {
                is BookingState.Loading -> {
                    LoadingStateScreen()
                }
                is BookingState.ActiveBidding -> {
                    ActiveBiddingScreen(
                        state = state,
                        onAcceptQuote = { quote -> viewModel.acceptQuote(quote) },
                        onNavigateBack = onNavigateBack
                    )
                }
                is BookingState.AwaitingPayment -> {
                    AwaitingPaymentScreen(
                        state = state,
                        onCancel = { viewModel.cancelPayment(requestId) },
                        onProceedToPayment = { /* MVP Dummy Navigation */ onNavigateToHome() }
                    )
                }
                is BookingState.Expired -> {
                    ExpiredScreen(
                        onReturnHome = onNavigateToHome
                    )
                }
                is BookingState.Completed -> {
                    CompletedScreen(
                        state = state,
                        onReturnHome = onNavigateToHome
                    )
                }
                is BookingState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.loadBiddingSession(requestId) },
                        onNavigateBack = onNavigateBack
                    )
                }
            }
        }

        // Overlay transparent blocking loading spinner
        if (actionState is ActionState.Loading) {
            Dialog(
                onDismissRequest = { }, 
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
