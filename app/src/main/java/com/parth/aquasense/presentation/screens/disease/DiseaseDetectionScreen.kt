package com.parth.aquasense.presentation.screens.disease

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun DiseaseDetectionScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit = {}, // Added simple back nav
    viewModel: DiseaseDetectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(false)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // CameraX Use Cases
    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var flashEnabled by remember { mutableStateOf(false) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageCaptured(uri)
            // Immediately analyze if tank is selected, otherwise wait for user
            // Logic: The original required tank selection. ViewState has selection.
            // If tank selected, we can probably proceed or show preview.
            // For now, let's just trigger capture in VM
        }
    }

    // Analyze on success
    LaunchedEffect(uiState) {
        if (uiState is DiseaseDetectionUiState.Success) {
            onNavigateToResults()
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Camera Preview Layer (Full Screen)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        cameraProvider = cameraProviderFuture.get()
                        try {
                            cameraProvider?.unbindAll()
                            camera = cameraProvider?.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        } catch (e: Exception) {
                            Log.e("CameraX", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // 2. Scanner Overlay Layer (Dimmed + Reticles)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanAreaSize = Size(size.width * 0.8f, size.width * 0.8f) // Square scan area
                val scanAreaTopLeft = Offset(
                    (size.width - scanAreaSize.width) / 2f,
                    (size.height - scanAreaSize.height) / 2f
                )

                // Draw semi-transparent background with cut-out
                val path = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = Rect(scanAreaTopLeft, scanAreaSize),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    )
                    fillType = PathFillType.EvenOdd
                }

                drawPath(
                    path = path,
                    color = Color.Black.copy(alpha = 0.5f)
                )

                // Draw Reticles (Frame Corners) - Matches provided reference image
                // 4 simple curved arcs at the corners, detached from each other.
                val strokeWidth = 2.dp.toPx()
                val cornerRadius = 24.dp.toPx() // Larger radius for smoother look
                val cap = StrokeCap.Round
                val reticleColor = Color.White.copy(alpha = 0.9f)

                // Top Left Arc
                drawPath(
                    path = Path().apply {
                        // Start slightly down on the left side
                        moveTo(scanAreaTopLeft.x, scanAreaTopLeft.y + cornerRadius)
                        // Arc to top center
                        quadraticBezierTo(
                            scanAreaTopLeft.x,
                            scanAreaTopLeft.y,
                            scanAreaTopLeft.x + cornerRadius,
                            scanAreaTopLeft.y
                        )
                    },
                    color = reticleColor,
                    style = Stroke(width = strokeWidth, cap = cap)
                )

                // Top Right Arc
                val topRight = Offset(scanAreaTopLeft.x + scanAreaSize.width, scanAreaTopLeft.y)
                drawPath(
                    path = Path().apply {
                         moveTo(topRight.x - cornerRadius, topRight.y)
                         quadraticBezierTo(
                             topRight.x,
                             topRight.y,
                             topRight.x,
                             topRight.y + cornerRadius
                         )
                    },
                    color = reticleColor,
                    style = Stroke(width = strokeWidth, cap = cap)
                )

                // Bottom Left Arc
                val bottomLeft = Offset(scanAreaTopLeft.x, scanAreaTopLeft.y + scanAreaSize.height)
                drawPath(
                    path = Path().apply {
                        moveTo(bottomLeft.x, bottomLeft.y - cornerRadius)
                        quadraticBezierTo(
                            bottomLeft.x,
                            bottomLeft.y,
                            bottomLeft.x + cornerRadius,
                            bottomLeft.y
                        )
                    },
                    color = reticleColor,
                    style = Stroke(width = strokeWidth, cap = cap)
                )

                // Bottom Right Arc
                val bottomRight = Offset(topRight.x, bottomLeft.y)
                drawPath(
                    path = Path().apply {
                        moveTo(bottomRight.x - cornerRadius, bottomRight.y)
                        quadraticBezierTo(
                            bottomRight.x,
                            bottomRight.y,
                            bottomRight.x,
                            bottomRight.y - cornerRadius
                        )
                    },
                    color = reticleColor,
                    style = Stroke(width = strokeWidth, cap = cap)
                )
            }

            // 3. UI Controls Layer
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(), // Handle insets for controls
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Disease Detection",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(onClick = {
                        flashEnabled = !flashEnabled
                        camera?.cameraControl?.enableTorch(flashEnabled)
                    }) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = Color.White
                        )
                    }
                }

                // New Tank Selector Position
                TankSelector(
                    tanks = viewState.tanks,
                    selectedTank = viewState.selectedTank,
                    onTankSelected = { viewModel.selectTank(it) },
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Guide Text

                Spacer(modifier = Modifier.weight(1f))

                // Guide Text
                Text(
                    text = "Align fish within the frame",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Control Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 120.dp, start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Button
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Gallery",
                            tint = Color.White
                        )
                    }

                    // Shutter Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .background(Color.White, CircleShape)
                            .clickable {
                                takePhoto(
                                    context = context,
                                    imageCapture = imageCapture,
                                    onImageCaptured = { uri ->
                                         viewModel.onImageCaptured(uri)
                                         viewModel.analyzeImage(context)
                                    },
                                    onError = { Log.e("Camera", "Capture failed", it) }
                                )
                            }
                    )

                    // Spacer to balance layout (or Search button as in Google Lens?)
                    // User asked for "Gallery" picker.
                    // Just put an invisible spacer of same size as gallery button to keep shutter centered
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Image Preview & Analysis State
             if (uiState is DiseaseDetectionUiState.Analyzing) {
                 Box(
                     modifier = Modifier
                         .fillMaxSize()
                         .background(Color.Black.copy(alpha = 0.7f)),
                     contentAlignment = Alignment.Center
                 ) {
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         CircularProgressIndicator(color = Color.White)
                         Spacer(modifier = Modifier.height(16.dp))
                         Text("Analyzing...", color = Color.White)
                     }
                 }
             }
        }
    } else {
        // Permission Denied / Requesting
        Box(
             modifier = Modifier.fillMaxSize(),
             contentAlignment = Alignment.Center
        ) {
             Text("Camera permission required.")
             Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                 Text("Grant Permission")
             }
        }
    }
}

@Composable
fun TankSelector(
    tanks: List<com.parth.aquasense.domain.model.Tank>,
    selectedTank: com.parth.aquasense.domain.model.Tank?,
    onTankSelected: (com.parth.aquasense.domain.model.Tank) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "GenericRotation"
    )

    val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    Column(
        modifier = modifier
            .width(260.dp) // Fixed width for better look
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) // Dark base
            .background(brush, RoundedCornerShape(16.dp)) // Glass sheen
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (selectedTank != null) {
                    Text(
                        text = "Selected Tank",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = selectedTank?.name ?: "Select Tank",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color.White,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )
        }

        // Expandable Content
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.expandVertically(
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(300)
            ),
            exit = androidx.compose.animation.shrinkVertically(
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeOut(
                animationSpec = androidx.compose.animation.core.tween(300)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // Limit height for list
                    .verticalScroll(rememberScrollState())
            ) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                
                tanks.forEach { tank ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTankSelected(tank)
                                expanded = false
                            }
                            .background(
                                if (selectedTank?.id == tank.id) Color.White.copy(alpha = 0.1f) else Color.Transparent
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Optional: Add species icon or circle here
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                     if (selectedTank?.id == tank.id) Color.Green else Color.Gray, 
                                     CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tank.name,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "camera_scan_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }

            override fun onError(exc: ImageCaptureException) {
                onError(exc)
            }
        }
    )
}
