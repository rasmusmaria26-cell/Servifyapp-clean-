package com.servify.app.feature.customer.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.servify.app.designsystem.theme.SpaceGrotesk
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationMapScreen(
    title: String,
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Location", 
                        fontFamily = SpaceGrotesk, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setMultiTouchControls(true)
                        val mapController = controller
                        mapController.setZoom(16.0)
                        
                        val startPoint = GeoPoint(latitude, longitude)
                        mapController.setCenter(startPoint)

                        val marker = Marker(this)
                        marker.position = startPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = title
                        marker.showInfoWindow()
                        overlays.add(marker)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    val point = GeoPoint(latitude, longitude)
                    view.controller.setCenter(point)
                    
                    // Update marker safely
                    view.overlays.clear()
                    val marker = Marker(view)
                    marker.position = point
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = title
                    marker.showInfoWindow()
                    view.overlays.add(marker)
                    
                    view.invalidate()
                }
            )
        }
    }
}
