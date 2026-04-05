package com.servify.app.feature.customer.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servify.app.designsystem.ServifyButton
import com.servify.app.designsystem.ShimmerItem
import com.servify.app.designsystem.theme.SpaceGrotesk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRepairRequestScreen(
    initialCategory: String? = null,
    viewModel: PostRepairRequestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSubmitted: (requestId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialCategory) {
        viewModel.initFromCategory(initialCategory)
    }

    // Handle back press — go to previous step instead of exiting
    BackHandler(enabled = uiState.currentStep > 1) {
        viewModel.prevStep()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Post Repair Request",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 1) viewModel.prevStep()
                        else onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier
            .padding(padding)
            .padding(horizontal = 20.dp)
            .imePadding()
            .verticalScroll(scrollState)
        ) {

            StepProgressIndicator(
                totalSteps = 3,
                currentStep = uiState.currentStep,
                completedSteps = uiState.completedSteps
            )

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> StepOneContent(
                        categoryFilter = initialCategory,
                        selectedDeviceType = uiState.deviceType,
                        onDeviceTypeSelected = viewModel::onDeviceType,
                        onNext = { if (viewModel.isCurrentStepValid()) viewModel.nextStep() }
                    )
                    2 -> StepTwoContent(
                        selectedDeviceType = uiState.deviceType,
                        selectedIssueCategory = uiState.issueCategory,
                        onIssueCategorySelected = viewModel::onIssueCategory,
                        onNext = { if (viewModel.isCurrentStepValid()) viewModel.nextStep() },
                        onBack = viewModel::prevStep
                    )
                    3 -> StepThreeContent(
                        selectedDeviceType = uiState.deviceType,
                        selectedIssueCategory = uiState.issueCategory,
                        description = uiState.description,
                        onDescriptionChanged = viewModel::onDescription,
                        isSubmitting = uiState.isSubmitting,
                        onSubmit = viewModel::submit,
                        onBack = viewModel::prevStep
                    )
                }
            }

            // Diagnosis result — shown after submission, before navigation
            if (uiState.submittedRequestId != null) {
                Spacer(modifier = Modifier.height(24.dp))
                when {
                    uiState.isDiagnosing -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Analysing your issue with AI…",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ShimmerItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                    uiState.diagnosis != null -> {
                        DiagnosisResultCard(diagnosis = uiState.diagnosis!!)
                        Spacer(modifier = Modifier.height(16.dp))
                        var hasNavigated by remember { mutableStateOf(false) }
                        ServifyButton(
                            text = "Continue →", 
                            onClick = {
                                if (!hasNavigated) {
                                    hasNavigated = true
                                    onSubmitted(uiState.submittedRequestId!!)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    uiState.diagnosisError != null -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Diagnosis unavailable. Continue to see vendor quotes.",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        var hasNavigated by remember { mutableStateOf(false) }
                        ServifyButton(
                            text = "Continue anyway →", 
                            onClick = {
                                if (!hasNavigated) {
                                    hasNavigated = true
                                    onSubmitted(uiState.submittedRequestId!!)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Show error message if validation fails randomly outside steps
            if (uiState.error != null && uiState.submittedRequestId == null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
