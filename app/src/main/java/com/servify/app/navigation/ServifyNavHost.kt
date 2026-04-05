package com.servify.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.AnimatedContentTransitionScope
import com.servify.app.core.AppMode
import com.servify.app.core.RenderCapabilities
import com.servify.app.feature.auth.presentation.LoginScreen
import com.servify.app.feature.customer.presentation.ActiveRepairScreen
import com.servify.app.feature.customer.presentation.BookingDetailScreen
import com.servify.app.feature.customer.presentation.CreateBookingScreen
import com.servify.app.feature.customer.presentation.CustomerDashboardScreen
import com.servify.app.feature.customer.presentation.CustomerDashboardViewModel
import com.servify.app.feature.customer.presentation.PostRepairRequestScreen
import com.servify.app.feature.customer.presentation.QuoteManagementScreen
import com.servify.app.feature.customer.presentation.HomeScreen
import com.servify.app.presentation.splash.SplashScreen
import com.servify.app.feature.vendor.presentation.RepairFeedScreen
import com.servify.app.feature.vendor.presentation.RepairFeedViewModel
import com.servify.app.feature.vendor.presentation.SubmitQuoteScreen
import com.servify.app.feature.vendor.presentation.VendorDashboardScreen
import com.servify.app.feature.customer.presentation.LocationMapScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

/**
 * Central NavHost for Servify.
 *
 * Transitions are mode-aware:
 * - Customer mode: cinematic 600ms slide+fade ([ServifyTransitions.customerEnterTransition])
 * - Vendor mode / reduceMotion: 150ms crossfade ([ServifyTransitions.vendorEnterTransition])
 *
 * This is the ONLY place routes are declared. Never define composable() routes in MainActivity
 * or any individual screen composable.
 */
@Composable
fun ServifyNavHost(
    navController: NavHostController,
    onRoleResolved: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val appMode by RenderCapabilities.appMode.collectAsStateWithLifecycle()
    val isCustomerMotion = appMode == AppMode.CUSTOMER && !RenderCapabilities.reduceMotion

    // Select transition specs based on current mode
    val enterTransition    = if (isCustomerMotion) ServifyTransitions.customerEnterTransition    else ServifyTransitions.vendorEnterTransition
    val exitTransition     = if (isCustomerMotion) ServifyTransitions.customerExitTransition     else ServifyTransitions.vendorExitTransition
    val popEnterTransition = if (isCustomerMotion) ServifyTransitions.customerPopEnterTransition else ServifyTransitions.vendorEnterTransition
    val popExitTransition  = if (isCustomerMotion) ServifyTransitions.customerPopExitTransition  else ServifyTransitions.vendorExitTransition

    NavHost(
        navController          = navController,
        startDestination       = ServifyRoutes.SPLASH,
        modifier               = modifier,
        enterTransition        = { enterTransition },
        exitTransition         = { exitTransition },
        popEnterTransition     = { popEnterTransition },
        popExitTransition      = { popExitTransition }
    ) {

        // --- Auth Flow ---

        composable(ServifyRoutes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(ServifyRoutes.LOGIN) {
                        popUpTo(ServifyRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = { role ->
                    if (role == "customer") {
                        navController.navigate(ServifyRoutes.CUSTOMER_GRAPH) {
                            popUpTo(ServifyRoutes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ServifyRoutes.home(role)) {
                            popUpTo(ServifyRoutes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(ServifyRoutes.LOGIN) {
            LoginScreen(
                onNavigateToSignup = { navController.navigate(ServifyRoutes.SIGNUP) },
                onLoginSuccess = { role ->
                    if (role == "customer") {
                        navController.navigate(ServifyRoutes.CUSTOMER_GRAPH) {
                            popUpTo(ServifyRoutes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ServifyRoutes.home(role)) {
                            popUpTo(ServifyRoutes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(ServifyRoutes.SIGNUP) {
            com.servify.app.feature.auth.presentation.SignupScreen(
                onNavigateToLogin  = { navController.popBackStack() },
                onSignupSuccess    = { role ->
                    if (role == "customer") {
                        navController.navigate(ServifyRoutes.CUSTOMER_GRAPH) {
                            popUpTo(ServifyRoutes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ServifyRoutes.home(role)) {
                            popUpTo(ServifyRoutes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        val customerTabs = listOf(
            ServifyRoutes.CUSTOMER_HOME,
            ServifyRoutes.CUSTOMER_ORDERS,
            ServifyRoutes.CUSTOMER_REPAIRS,
            ServifyRoutes.CUSTOMER_PROFILE
        )

        fun getSlideDirection(scope: AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>): Boolean {
            val initialRoute = scope.initialState.destination.route ?: return true
            val targetRoute = scope.targetState.destination.route ?: return true
            
            val initialIndex = customerTabs.indexOf(initialRoute)
            val targetIndex = customerTabs.indexOf(targetRoute)
            
            // If dragging from a non-tab, default to sliding left (moving forward).
            if (initialIndex == -1 || targetIndex == -1) return true
            
            // True if sliding leftwards (target is to the right of initial)
            return targetIndex > initialIndex
        }

        val slideDuration = 400

        navigation(startDestination = ServifyRoutes.CUSTOMER_HOME, route = ServifyRoutes.CUSTOMER_GRAPH) {
            composable(
                route = ServifyRoutes.CUSTOMER_HOME,
                enterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                exitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                },
                popEnterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                popExitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                }
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(ServifyRoutes.CUSTOMER_GRAPH) }
                val viewModel = hiltViewModel<CustomerDashboardViewModel>(parentEntry)
                CustomerDashboardScreen(
                    selectedTab = 0,
                    viewModel = viewModel,
                    onNavigateToBooking = { cat -> navController.navigate(ServifyRoutes.createBooking(cat)) },
                    onNavigateToRepairRequest = { navController.navigate(ServifyRoutes.postRepairRequest()) },
                    onNavigateToQuotes = { id -> navController.navigate(ServifyRoutes.quotes(id)) },
                    onNavigateToActiveRepair = { id -> navController.navigate(ServifyRoutes.activeRepair(id)) },
                    onNavigateToBookingDetail = { id -> navController.navigate(ServifyRoutes.bookingDetail(id)) },
                    onSignOut = {
                        navController.navigate(ServifyRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
            composable(
                route = ServifyRoutes.CUSTOMER_ORDERS,
                enterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                exitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                },
                popEnterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                popExitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                }
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(ServifyRoutes.CUSTOMER_GRAPH) }
                val viewModel = hiltViewModel<CustomerDashboardViewModel>(parentEntry)
                CustomerDashboardScreen(
                    selectedTab = 1,
                    viewModel = viewModel,
                    onNavigateToBooking = { cat -> navController.navigate(ServifyRoutes.createBooking(cat)) },
                    onNavigateToRepairRequest = { navController.navigate(ServifyRoutes.postRepairRequest()) },
                    onNavigateToQuotes = { id -> navController.navigate(ServifyRoutes.quotes(id)) },
                    onNavigateToActiveRepair = { id -> navController.navigate(ServifyRoutes.activeRepair(id)) },
                    onNavigateToBookingDetail = { id -> navController.navigate(ServifyRoutes.bookingDetail(id)) },
                    onSignOut = {
                        navController.navigate(ServifyRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
            composable(
                route = ServifyRoutes.CUSTOMER_REPAIRS,
                enterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                exitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                },
                popEnterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                popExitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                }
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(ServifyRoutes.CUSTOMER_GRAPH) }
                val viewModel = hiltViewModel<CustomerDashboardViewModel>(parentEntry)
                CustomerDashboardScreen(
                    selectedTab = 2,
                    viewModel = viewModel,
                    onNavigateToBooking = { cat -> navController.navigate(ServifyRoutes.createBooking(cat)) },
                    onNavigateToRepairRequest = { navController.navigate(ServifyRoutes.postRepairRequest()) },
                    onNavigateToQuotes = { id -> navController.navigate(ServifyRoutes.quotes(id)) },
                    onNavigateToActiveRepair = { id -> navController.navigate(ServifyRoutes.activeRepair(id)) },
                    onNavigateToBookingDetail = { id -> navController.navigate(ServifyRoutes.bookingDetail(id)) },
                    onSignOut = {
                        navController.navigate(ServifyRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
            composable(
                route = ServifyRoutes.CUSTOMER_PROFILE,
                enterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                exitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                },
                popEnterTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorEnterTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (isMovingRight) fullWidth else -fullWidth },
                            animationSpec = tween(slideDuration)
                        ) + fadeIn(animationSpec = tween(slideDuration))
                    }
                },
                popExitTransition = {
                    if (!isCustomerMotion) ServifyTransitions.vendorExitTransition else {
                        val isMovingRight = getSlideDirection(this)
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (isMovingRight) -fullWidth / 3 else fullWidth / 3 },
                            animationSpec = tween(slideDuration)
                        ) + fadeOut(animationSpec = tween(slideDuration / 2))
                    }
                }
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(ServifyRoutes.CUSTOMER_GRAPH) }
                val viewModel = hiltViewModel<CustomerDashboardViewModel>(parentEntry)
                CustomerDashboardScreen(
                    selectedTab = 3,
                    viewModel = viewModel,
                    onNavigateToBooking = { cat -> navController.navigate(ServifyRoutes.createBooking(cat)) },
                    onNavigateToRepairRequest = { navController.navigate(ServifyRoutes.postRepairRequest()) },
                    onNavigateToQuotes = { id -> navController.navigate(ServifyRoutes.quotes(id)) },
                    onNavigateToActiveRepair = { id -> navController.navigate(ServifyRoutes.activeRepair(id)) },
                    onNavigateToBookingDetail = { id -> navController.navigate(ServifyRoutes.bookingDetail(id)) },
                    onSignOut = {
                        navController.navigate(ServifyRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        }

        // Keep the role-based route for Vendors to use
        composable(ServifyRoutes.HOME) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "customer"
            onRoleResolved(role)
            if (role.equals("vendor", ignoreCase = true)) {
                VendorDashboardScreen(
                    onNavigateToRepairFeed = { navController.navigate(ServifyRoutes.REPAIR_FEED) },
                    onSignOut = {
                        navController.navigate(ServifyRoutes.LOGIN) { popUpTo(0) { inclusive = true } }
                    },
                    onNavigateToMap = { title, lat, lng ->
                        navController.navigate(ServifyRoutes.locationMap(title, lat, lng))
                    }
                )
            } else {
                // If by some chance we end up on home/customer, just fallback to HomeScreen until the Graph is re-evaluated.
                HomeScreen(role = role)
            }
        }

        // --- Customer Screens ---

        composable(
            route = ServifyRoutes.CREATE_BOOKING,
            arguments = listOf(navArgument("category") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            CreateBookingScreen(
                initialCategory  = category,
                onNavigateBack   = { navController.popBackStack() },
                onBookingCreated = { navController.popBackStack() }
            )
        }

        composable(ServifyRoutes.BOOKING_DETAIL) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: return@composable
            val viewModel = hiltViewModel<com.servify.app.feature.customer.presentation.BookingDetailViewModel>()
            
            androidx.compose.runtime.LaunchedEffect(bookingId) {
                viewModel.fetchBooking(bookingId)
            }
            
            BookingDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = { name, lat, lng ->
                    navController.navigate(ServifyRoutes.locationMap(name, lat, lng))
                }
            )
        }

        composable(
            route = ServifyRoutes.POST_REPAIR_REQUEST,
            arguments = listOf(navArgument("category") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            PostRepairRequestScreen(
                initialCategory = category,
                onNavigateBack = { navController.popBackStack() },
                onSubmitted    = { requestId ->
                    navController.navigate(ServifyRoutes.quotes(requestId)) {
                        popUpTo(ServifyRoutes.POST_REPAIR_REQUEST) { inclusive = true }
                    }
                }
            )
        }

        composable(ServifyRoutes.QUOTES) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: return@composable
            QuoteManagementScreen(
                requestId      = requestId,
                onNavigateBack = { navController.popBackStack() },
                onQuoteAccepted = {
                    navController.navigate(ServifyRoutes.activeRepair(requestId)) {
                        popUpTo(ServifyRoutes.quotes(requestId)) { inclusive = true }
                    }
                }
            )
        }

        composable(ServifyRoutes.ACTIVE_REPAIR) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: return@composable
            ActiveRepairScreen(
                requestId      = requestId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ServifyRoutes.LOCATION_MAP,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lng") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Location"
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lng = backStackEntry.arguments?.getFloat("lng")?.toDouble() ?: 0.0
            LocationMapScreen(
                title = title,
                latitude = lat,
                longitude = lng,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Vendor Screens ---

        composable(ServifyRoutes.REPAIR_FEED) {
            RepairFeedScreen(
                onNavigateToSubmitQuote = { navController.navigate(ServifyRoutes.SUBMIT_QUOTE) }
            )
        }

        composable(ServifyRoutes.SUBMIT_QUOTE) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(ServifyRoutes.REPAIR_FEED)
            }
            val feedViewModel: RepairFeedViewModel = hiltViewModel(parentEntry)
            SubmitQuoteScreen(
                viewModel       = feedViewModel,
                onNavigateBack  = { navController.popBackStack() },
                onQuoteSubmitted = {
                    navController.navigate(ServifyRoutes.REPAIR_FEED) {
                        popUpTo(ServifyRoutes.REPAIR_FEED) { inclusive = false }
                    }
                },
                onNavigateToMap = { title, lat, lng ->
                    navController.navigate(ServifyRoutes.locationMap(title, lat, lng))
                }
            )
        }
    }
}
