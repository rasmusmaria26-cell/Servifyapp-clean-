package com.servify.app.core.payment

data class PaymentCallbacks(
    val onSuccess: (String?) -> Unit,
    val onError: (String) -> Unit,
    val onDismissed: () -> Unit
)

object PaymentResultDispatcher {
    private val lock = Any()
    private var callbacks: PaymentCallbacks? = null

    fun register(newCallbacks: PaymentCallbacks) {
        synchronized(lock) {
            callbacks = newCallbacks
        }
    }

    fun clear() {
        synchronized(lock) {
            callbacks = null
        }
    }

    fun dispatchSuccess(paymentId: String?) {
        val current = synchronized(lock) {
            callbacks.also { callbacks = null }
        } ?: return

        current.onSuccess(paymentId)
    }

    fun dispatchError(code: Int, response: String?) {
        val current = synchronized(lock) {
            callbacks.also { callbacks = null }
        } ?: return

        val message = response.orEmpty()
        val isDismissed = code == 2 ||
            message.contains("cancel", ignoreCase = true) ||
            message.contains("dismiss", ignoreCase = true) ||
            message.contains("back pressed", ignoreCase = true)

        if (isDismissed) {
            current.onDismissed()
        } else {
            current.onError("Payment failed - Code: $code, Message: $response")
        }
    }
}
