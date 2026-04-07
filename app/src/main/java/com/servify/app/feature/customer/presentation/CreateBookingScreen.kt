package com.servify.app.feature.customer.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.servify.app.feature.vendor.domain.Vendor
import com.servify.app.designsystem.ServifyButton
import com.servify.app.designsystem.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBookingScreen(
    initialCategory: String? = null,
    viewModel: CreateBookingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onBookingCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialCategory) {
        if (initialCategory != null && uiState.selectedServiceCategory == null) {
            viewModel.onServiceCategorySelected(initialCategory)
        }
    }

    LaunchedEffect(uiState.bookingCreated) {
        if (uiState.bookingCreated) {
            onBookingCreated()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "New Booking",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Step Progress Indicator
            BookingStepIndicator(currentStep = uiState.currentStep)

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content with Crossfade
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(
                    targetState = uiState.currentStep,
                    animationSpec = tween(300),
                    label = "step_crossfade"
                ) { step ->
                    when (step) {
                        1 -> IssueDescriptionStep(
                            description = uiState.issueDescription,
                            selectedCategory = uiState.selectedServiceCategory,
                            selectedBitmaps = uiState.selectedBitmaps,
                            onDescriptionChange = viewModel::onDescriptionChange,
                            onCategorySelected = viewModel::onServiceCategorySelected,
                            onImagesSelected = viewModel::onImagesSelected,
                            onNextClick = viewModel::onNextStep
                        )
                        2 -> VendorSelectionStep(
                            vendors = uiState.matchedVendors,
                            isLoading = uiState.isLoadingVendors,
                            isLoadingDiagnosis = uiState.isLoadingDiagnosis,
                            aiDiagnosis = uiState.aiDiagnosis,
                            selectedVendor = uiState.selectedVendor,
                            onVendorSelected = viewModel::onVendorSelected,
                            onNextClick = viewModel::onNextStep
                        )
                        3 -> SchedulingStep(
                            selectedDate = uiState.selectedDate,
                            selectedTime = uiState.selectedTime,
                            address = uiState.address,
                            latitude = uiState.latitude,
                            longitude = uiState.longitude,
                            onDateSelected = viewModel::onDateSelected,
                            onTimeSelected = viewModel::onTimeSelected,
                            onAddressChange = viewModel::onAddressChange,
                            onLocationSelected = viewModel::onLocationSelected,
                            onNextClick = viewModel::onNextStep
                        )
                        4 -> ConfirmationStep(
                            uiState = uiState,
                            onConfirmClick = viewModel::onConfirmBooking
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ==========================================================
// Step Indicator — ServifyBlue active, dark inactive
// ==========================================================
@Composable
fun BookingStepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..4) {
            val isActive = i <= currentStep
            val isCurrent = i == currentStep

            val animatedSize by animateDpAsState(
                targetValue = if (isCurrent) 36.dp else 28.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "step_size"
            )

            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCurrent -> ServifyBlue
                            isActive -> ServifyBlue.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .then(
                        if (isCurrent) Modifier.border(2.dp, ServifyBlue.copy(alpha = 0.5f), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isActive && !isCurrent) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = i.toString(),
                        color = if (isActive) Color.White else TextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (i < 4) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    color = if (i < currentStep) ServifyBlue.copy(alpha = 0.4f) else DarkBorder,
                    thickness = 2.dp
                )
            }
        }
    }
}

// ==========================================================
// Step 1: Issue Description
// ==========================================================
@Composable
fun IssueDescriptionStep(
    description: String,
    selectedCategory: String?,
    selectedBitmaps: List<Bitmap>,
    onDescriptionChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onImagesSelected: (List<Bitmap>) -> Unit,
    onNextClick: () -> Unit
) {
    val categories = listOf("Home Appliances", "Electronics", "Vehicles", "Electrical", "Plumbing", "Carpentry", "AC / HVAC", "More")

    val darkFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ServifyBlue,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = ServifyBlue,
        unfocusedLabelColor = TextSecondary,
        cursorColor = ServifyBlue,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Describe the Issue",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tell us what's wrong and select a service category.",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Inter,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Service Category",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ServifyBlue.copy(alpha = 0.2f),
                        selectedLabelColor = ServifyBlue,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = ServifyBlue,
                        enabled = true,
                        selected = selectedCategory == category
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedCategory != null) {
            val subIssues = when (selectedCategory) {
                "Electronics" -> listOf("Screen / Display", "Battery / Power", "Audio / Speaker", "Software", "Water Damage", "Other")
                "Electrical" -> listOf("Wiring / Short Circuit", "Appliance Repair", "Switchboard", "Lighting", "Inverter", "Other")
                "Plumbing" -> listOf("Pipe Leakage", "Faucet / Tap", "Drain Blockage", "Water Heater", "Toilet/Sanitary", "Other")
                "AC / HVAC" -> listOf("Not Cooling", "Gas Leak", "Making Noise", "Water Dripping", "Coil/Compressor", "Other")
                "Carpentry" -> listOf("Furniture Repair", "Door/Window", "Wood Polishing", "Custom Assembly", "Lock/Hinge", "Other")
                "Home Appliances" -> listOf("Washing Machine", "Refrigerator", "Microwave", "Dishwasher", "Water Purifier", "Other")
                "Vehicles" -> listOf("Two-Wheeler", "Four-Wheeler", "Tire / Puncture", "Engine / Oil", "Electricals", "Other")
                else -> listOf("Performance Issue", "Physical Damage", "Part Replacement", "Diagnostic Check", "Other")
            }

            Text(
                text = "What is the main issue?",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subIssues.forEach { issue ->
                    SuggestionChip(
                        onClick = { onDescriptionChange(issue) },
                        label = { Text(issue, fontFamily = Inter) },
                        shape = RoundedCornerShape(16.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = TextSecondary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Issue Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(14.dp),
            colors = darkFieldColors
        )

        val context = LocalContext.current
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            val bitmaps = uris.mapNotNull { uri ->
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }
            onImagesSelected(bitmaps)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedBitmaps.isEmpty()) "Add photos (optional)" else "${selectedBitmaps.size} photo(s) added",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ServifyButton(
            text = "Find Vendors",
            onClick = onNextClick,
            enabled = description.isNotBlank() && selectedCategory != null
        )
    }
}

// ==========================================================
// Step 2: Vendor Selection
// ==========================================================
@Composable
fun VendorSelectionStep(
    vendors: List<Vendor>,
    isLoading: Boolean,
    isLoadingDiagnosis: Boolean,
    aiDiagnosis: com.servify.app.core.model.AIDiagnosis?,
    selectedVendor: Vendor?,
    onVendorSelected: (Vendor) -> Unit,
    onNextClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select a Vendor",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (isLoadingDiagnosis) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = ServifyBlue,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Analysing your issue with AI...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        } else if (aiDiagnosis != null) {
            // Moved to LazyColumn to show full DiagnosisResultCard
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (aiDiagnosis != null && !isLoadingDiagnosis) {
                    item {
                        DiagnosisResultCard(diagnosis = aiDiagnosis)
                    }
                }

                if (vendors.isEmpty()) {
                    item {
                        Text(
                            text = "No vendors found for this category.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(vendors) { vendor ->
                        VendorCard(
                            vendor = vendor,
                            isSelected = vendor == selectedVendor,
                            onSelect = { onVendorSelected(vendor) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ServifyButton(
            text = "Continue",
            onClick = onNextClick,
            enabled = selectedVendor != null
        )
    }
}

@Composable
fun VendorCard(
    vendor: Vendor,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "vendor_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(1.5.dp, ServifyBlue) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ServifyBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vendor.businessName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${vendor.rating} (${vendor.totalReviews})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${vendor.hourlyRate}/hr",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    fontFamily = Inter,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ==========================================================
// Step 3: Scheduling
// ==========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingStep(
    selectedDate: String,
    selectedTime: String,
    address: String,
    latitude: Double?,
    longitude: Double?,
    onDateSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onLocationSelected: (Double, Double) -> Unit,
    onNextClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val darkFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ServifyBlue,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = ServifyBlue,
        unfocusedLabelColor = TextSecondary,
        cursorColor = ServifyBlue,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onDateSelected(formatter.format(date))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    onTimeSelected(formattedTime)
                    showTimePicker = false
                }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Schedule Service",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick a convenient time for the service.",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Inter,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Date Field
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Service Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Event, contentDescription = "Select Date", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = darkFieldColors
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Field
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { },
                    label = { Text("Service Time") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Select Time", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = darkFieldColors
                )
                Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Map View
            val context = LocalContext.current
            val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
            
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                onLocationSelected(location.latitude, location.longitude)
                            }
                        }
                    } catch (e: SecurityException) { }
                }
            }
            
            fun requestLocation() {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                onLocationSelected(location.latitude, location.longitude)
                            }
                        }
                    } catch (e: SecurityException) { }
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }

            LaunchedEffect(Unit) {
                Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            val initialPoint = GeoPoint(latitude ?: 28.6139, longitude ?: 77.2090)
                            controller.setCenter(initialPoint)

                            addMapListener(object : org.osmdroid.events.MapListener {
                                override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                                    val center = mapCenter
                                    onLocationSelected(center.latitude, center.longitude)
                                    return true
                                }
                                override fun onZoom(event: org.osmdroid.events.ZoomEvent?) = false
                            })
                        }
                    },
                    update = { view ->
                        if (latitude != null && longitude != null) {
                            val dist = Math.abs(view.mapCenter.latitude - latitude) + Math.abs(view.mapCenter.longitude - longitude)
                            if (dist > 0.0001) {
                                view.controller.animateTo(GeoPoint(latitude, longitude))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Pinpoint",
                    tint = ErrorRed,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .offset(y = (-18).dp)
                )
                
                // Locate Me Button
                SmallFloatingActionButton(
                    onClick = { requestLocation() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = ServifyBlue,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Locate Me")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Address Field
            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Service Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = darkFieldColors
            )

            Spacer(modifier = Modifier.height(24.dp))
        } // End of scrolling column

        ServifyButton(
            text = "Review & Confirm",
            onClick = onNextClick,
            enabled = selectedDate.isNotBlank() && selectedTime.isNotBlank() && address.isNotBlank()
        )
    }
}

// ==========================================================
// Step 4: Confirmation
// ==========================================================
@Composable
fun ConfirmationStep(
    uiState: CreateBookingUiState,
    onConfirmClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Review Booking",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    LabelValueRow("Service", uiState.selectedServiceCategory ?: "N/A")
                    LabelValueRow("Vendor", uiState.selectedVendor?.businessName ?: "N/A")
                    LabelValueRow("Date", uiState.selectedDate)
                    LabelValueRow("Time", uiState.selectedTime)
                    LabelValueRow("Address", uiState.address)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    LabelValueRow("Rate", "₹${uiState.selectedVendor?.hourlyRate ?: 0}/hr", isBold = true)
                }
            }

            uiState.aiDiagnosis?.let { diagnosis ->
                Spacer(modifier = Modifier.height(16.dp))

                val urgencyColor = when (diagnosis.urgency) {
                    "High" -> ErrorRed
                    "Medium" -> AmberAccent
                    else -> SuccessGreen
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI Diagnosis",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = SpaceGrotesk
                            )
                            Box(
                                modifier = Modifier
                                    .background(urgencyColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = diagnosis.urgency,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = urgencyColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(diagnosis.diagnosis, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(diagnosis.urgencyReason, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                        LabelValueRow("Est. cost", diagnosis.estimatedCost)
                        LabelValueRow("Est. time", diagnosis.estimatedTime)

                        if (diagnosis.customerAdvice.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AmberAccent.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "Tip", style = MaterialTheme.typography.labelSmall, color = AmberAccent, fontWeight = FontWeight.Bold)
                                Text(text = diagnosis.customerAdvice, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        } // End of scrolling column

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ServifyButton(
            text = "Confirm Booking",
            onClick = onConfirmClick,
            isLoading = uiState.isCreatingBooking,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun LabelValueRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontFamily = Inter, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = Inter,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun DiagnosisSummaryChip(diagnosis: com.servify.app.core.model.AIDiagnosis) {
    val urgencyColor = when (diagnosis.urgency) {
        "High" -> ErrorRed
        "Medium" -> AmberAccent
        else -> SuccessGreen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, urgencyColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI Diagnosis",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = diagnosis.diagnosis,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .background(urgencyColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = diagnosis.urgency,
                style = MaterialTheme.typography.labelSmall,
                color = urgencyColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

