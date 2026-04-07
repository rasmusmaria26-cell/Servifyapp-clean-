package com.servify.app.core.payment

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val paymentId: String? = null,
    val isDismissed: Boolean = false
)

/**
 * ViewModel for managing payment states
 */
class PaymentViewModel : ViewModel() {
    
    private val _paymentState = MutableStateFlow(PaymentState())
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    fun handlePaymentSuccess(paymentId: String?) {
        _paymentState.value = PaymentState(
            isLoading = false,
            isSuccess = true,
            paymentId = paymentId ?: "UNKNOWN"
        )
    }

    fun handlePaymentError(error: String) {
        _paymentState.value = PaymentState(
            isLoading = false,
            errorMessage = error
        )
    }

    fun handlePaymentDismissed() {
        _paymentState.value = PaymentState(
            isLoading = false,
            isDismissed = true
        )
    }

    fun resetPaymentState() {
        PaymentResultDispatcher.clear()
        _paymentState.value = PaymentState()
    }

    fun startPaymentProcess(
        context: Context,
        amount: Long,
        description: String,
        customerName: String,
        customerEmail: String,
        customerPhone: String
    ) {
        if (_paymentState.value.isLoading) return

        viewModelScope.launch {
            _paymentState.value = PaymentState(isLoading = true)

            PaymentResultDispatcher.register(
                PaymentCallbacks(
                    onSuccess = { paymentId -> handlePaymentSuccess(paymentId) },
                    onError = { error -> handlePaymentError(error) },
                    onDismissed = { handlePaymentDismissed() }
                )
            )

            val paymentManager = RazorpayPaymentManager(
                context = context
            )
            try {
                paymentManager.startPayment(
                    amount = amount,
                    description = description,
                    customerName = customerName,
                    customerEmail = customerEmail,
                    customerPhone = customerPhone
                )
            } catch (e: Exception) {
                PaymentResultDispatcher.clear()
                handlePaymentError(e.message ?: "Payment initialization failed")
            }
        }
    }

    override fun onCleared() {
        PaymentResultDispatcher.clear()
        super.onCleared()
    }
}
