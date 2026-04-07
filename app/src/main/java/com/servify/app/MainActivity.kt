package com.servify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.servify.app.core.UserSession
import com.servify.app.navigation.ServifyBottomNavBar
import com.servify.app.navigation.ServifyNavHost
import com.servify.app.navigation.ServifyRoutes
import com.servify.app.designsystem.theme.ServifyTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultListener
import com.razorpay.PaymentResultWithDataListener
import com.servify.app.core.payment.PaymentResultDispatcher
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity entry point for Servify.
 *
 * Responsibilities:
 * - Enables edge-to-edge rendering (glass layers handle their own insets).
 * - Wraps everything in [ServifyTheme] which reacts to [RenderCapabilities.appMode].
 * - Hosts [ServifyNavHost] (all route declarations) and [ServifyBottomNavBar].
 * - Propagates the resolved user role to [UserSession] to trigger mode-switching.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultListener, PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ServifyTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in listOf(
                    ServifyRoutes.CUSTOMER_HOME,
                    ServifyRoutes.CUSTOMER_ORDERS,
                    ServifyRoutes.CUSTOMER_REPAIRS,
                    ServifyRoutes.CUSTOMER_PROFILE,
                    ServifyRoutes.HOME,
                    ServifyRoutes.REPAIR_FEED
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            ServifyBottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    ServifyNavHost(
                        navController = navController,
                        onRoleResolved = { role ->
                            // When the NavHost resolves the authenticated user role,
                            // propagate it to UserSession so RenderCapabilities.appMode
                            // emits the correct value and ServifyTheme recomposes.
                            val mode = if (role.equals("vendor", ignoreCase = true))
                                com.servify.app.core.AppMode.VENDOR
                            else
                                com.servify.app.core.AppMode.CUSTOMER
                            UserSession.switchMode(mode)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        PaymentResultDispatcher.dispatchSuccess(razorpayPaymentId)
    }
    override fun onPaymentError(code: Int, response: String?) {
        PaymentResultDispatcher.dispatchError(code, response)
    }
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        PaymentResultDispatcher.dispatchSuccess(razorpayPaymentId)
    }
    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        PaymentResultDispatcher.dispatchError(code, response)
    }
}