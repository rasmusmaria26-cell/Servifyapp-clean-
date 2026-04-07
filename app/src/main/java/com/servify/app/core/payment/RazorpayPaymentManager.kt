package com.servify.app.core.payment

import android.content.Context
import androidx.activity.ComponentActivity
import com.razorpay.Checkout
import com.servify.app.BuildConfig
import org.json.JSONObject

/**
 * Razorpay Payment Manager for handling local test mode payments
 * This is configured for LOCAL TESTING ONLY using Razorpay Test Keys
 */
class RazorpayPaymentManager(
    private val context: Context
) {

    init {
        // Initialize Checkout in Activity context
        Checkout.preload(context)
    }

    /**
     * Start payment process using Razorpay Test Key
     *
     * @param amount Amount in paisa (e.g., 50000 = ₹500)
     * @param description Payment description
     * @param customerName Customer name
     * @param customerEmail Customer email
     * @param customerPhone Customer phone (10 digits)
     */
    fun startPayment(
        amount: Long,
        description: String = "Servify Service Payment",
        customerName: String,
        customerEmail: String,
        customerPhone: String
    ) {
        val activity = context as? ComponentActivity
            ?: throw IllegalStateException("Payment requires an Activity context")

        if (BuildConfig.RAZORPAY_TEST_KEY_ID.isBlank()) {
            throw IllegalStateException("Razorpay test key is missing. Add RAZORPAY_TEST_KEY_ID in local.properties")
        }

        try {
            val checkout = Checkout()
            checkout.setKeyID(BuildConfig.RAZORPAY_TEST_KEY_ID)

            val options = JSONObject().apply {
                put("key", BuildConfig.RAZORPAY_TEST_KEY_ID)
                put("amount", amount)
                put("currency", "INR")
                put("name", "Servify")
                put("description", description)
                put("timeout", 600)

                put("prefill", JSONObject().apply {
                    put("name", customerName)
                    put("email", customerEmail)
                    put("contact", customerPhone)
                })

                put("theme", JSONObject().apply {
                    put("color", "#3399cc")
                })

                put("retry", JSONObject().apply {
                    put("enabled", true)
                    put("max_count", 2)
                })
            }

            checkout.open(activity, options)
        } catch (e: Exception) {
            throw IllegalStateException("Payment initialization failed: ${e.message}", e)
        }
    }

    companion object {
        /**
         * Test card details for local testing:
         *
         * Visa Test Card:
         *   Number: 4111 1111 1111 1111
         *   Expiry: Any future date (MM/YY)
         *   CVV: Any 3 digits
         *
         * Test UPI:
         *   success@razorpay (for successful payment)
         *   fail@razorpay (for failed payment)
         *
         * Test Netbanking:
         *   Select any bank - payment will succeed in test mode
         *
         * Note: Use ANY amount in test mode, payments are NOT charged
         */
        const val TEST_AMOUNT_500 = 50000L   // ₹500
        const val TEST_AMOUNT_1000 = 100000L // ₹1000
        const val TEST_AMOUNT_2000 = 200000L // ₹2000
    }
}
