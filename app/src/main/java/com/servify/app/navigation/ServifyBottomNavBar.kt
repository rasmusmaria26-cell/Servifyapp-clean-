package com.servify.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.servify.app.core.AppMode
import com.servify.app.core.RenderCapabilities
import com.servify.app.designsystem.theme.GLASS_ALPHA_DARK
import com.servify.app.designsystem.theme.GLASS_ALPHA_LIGHT
import com.servify.app.designsystem.theme.SOLID_ALPHA_DARK
import com.servify.app.designsystem.theme.SOLID_ALPHA_LIGHT

/**
 * Dual-mode bottom navigation bar.
 *
 * NOTE on glassmorphism: Modifier.blur() blurs the composable's OWN content,
 * not the content behind it (no backdrop-filter equivalent in Compose).
 * True frosted glass requires RenderEffect on a background layer — planned for Phase 17.
 * For now: opaque surface with top divider as visual separator.
 *
 * Collects [RenderCapabilities.appMode] as Compose state first.
 * DO NOT compare the StateFlow inline — always collect then use.
 */
@Composable
fun ServifyBottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Collect appMode as Compose state — NEVER compare StateFlow inline.
    val appMode by RenderCapabilities.appMode.collectAsStateWithLifecycle()
    val isCustomer = appMode == AppMode.CUSTOMER

    // Dynamic glassmorphism based on hardware capabilities and AppMode
    val useBlur = RenderCapabilities.supportsBlur && isCustomer
    
    // Alphas from Design System Architecture
    val glassAlpha = if (isCustomer) GLASS_ALPHA_LIGHT else GLASS_ALPHA_DARK
    val solidAlpha = if (isCustomer) SOLID_ALPHA_LIGHT else SOLID_ALPHA_DARK
    
    val containerColor = MaterialTheme.colorScheme.surface.copy(
        alpha = if (useBlur) glassAlpha else solidAlpha
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    // Build nav items based on current mode
    val items = if (isCustomer) customerNavItems() else vendorNavItems()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .windowInsetsPadding(WindowInsets.navigationBars) // Edge-to-edge mandatory
    ) {
        // Subtle top divider for visual separation (replaces blur effect for now)
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected  = selected,
                    onClick   = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector        = item.icon,
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            text  = item.label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = MaterialTheme.colorScheme.primary,
                        selectedTextColor   = MaterialTheme.colorScheme.primary,
                        indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), // Sharper, more vibrant indicator
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            } // end forEach
        } // end NavigationBar
    } // end Column
}

data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private fun customerNavItems() = listOf(
    NavItem(ServifyRoutes.CUSTOMER_HOME,    "Home",    Icons.Filled.Home),
    NavItem(ServifyRoutes.CUSTOMER_ORDERS,  "Orders",  Icons.Filled.List),
    NavItem(ServifyRoutes.CUSTOMER_REPAIRS, "Repairs", Icons.Filled.Build),
    NavItem(ServifyRoutes.CUSTOMER_PROFILE, "Profile", Icons.Filled.Person)
)

private fun vendorNavItems() = listOf(
    NavItem(ServifyRoutes.HOME.replace("{role}", "vendor"), "Dashboard", Icons.Filled.Dashboard),
    NavItem(ServifyRoutes.REPAIR_FEED,                      "Feed",      Icons.Filled.Search),
)
