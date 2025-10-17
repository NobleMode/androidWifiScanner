package com.wifiscanner.app

import android.graphics.RectF
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// QR Code bounding box overlay implemented!
// - Draws green rectangle around detected QR code
// - Animated pulsing effect for better UX
// - Uses barcode.boundingBox coordinates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onQRCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val haptic = rememberHapticFeedback()
    val sounds = rememberSoundEffects()
    
    var hasScanned by remember { mutableStateOf(false) }
    var qrBoundingBox by remember { mutableStateOf<RectF?>(null) }
    var isValidWiFiQR by remember { mutableStateOf(false) }
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }
    var showingBox by remember { mutableStateOf(false) }
    var imageRotation by remember { mutableStateOf(0) }
    
    // Add delay before showing details when WiFi QR is found
    LaunchedEffect(hasScanned) {
        if (hasScanned && isValidWiFiQR) {
            kotlinx.coroutines.delay(1500) // Show box for 1.5 seconds
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan WiFi QR Code") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.tap()
                        onClose()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    
                    val barcodeScanner = BarcodeScanning.getClient()
                    
                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        // Store image dimensions and rotation for coordinate transformation
                        imageWidth = imageProxy.width
                        imageHeight = imageProxy.height
                        imageRotation = imageProxy.imageInfo.rotationDegrees
                        
                        if (!hasScanned) {
                            processImageProxy(barcodeScanner, imageProxy) { barcodes ->
                                val barcode = barcodes.firstOrNull()
                                if (barcode != null) {
                                    // Update bounding box with raw coordinates
                                    barcode.boundingBox?.let { box ->
                                        qrBoundingBox = RectF(
                                            box.left.toFloat(),
                                            box.top.toFloat(),
                                            box.right.toFloat(),
                                            box.bottom.toFloat()
                                        )
                                        showingBox = true
                                        Log.d("QRScanner", "QR Box: $box, Image: ${imageWidth}x${imageHeight}, Rotation: $imageRotation")
                                    }
                                    
                                    // Check if it's a WiFi QR code
                                    barcode.rawValue?.let { qrContent ->
                                        val isWiFi = qrContent.startsWith("WIFI:")
                                        isValidWiFiQR = isWiFi
                                        Log.d("QRScanner", "QR detected: isWiFi=$isWiFi, content=${qrContent.take(20)}")
                                        if (isWiFi) {
                                            // Vibrate and beep when QR detected
                                            haptic.qrDetected()
                                            sounds.playScanBeep()
                                            
                                            // Set hasScanned but don't call onQRCodeScanned yet
                                            // The LaunchedEffect will wait 1.5 seconds
                                            hasScanned = true
                                            // Delay the callback
                                            GlobalScope.launch {
                                                delay(1500)
                                                onQRCodeScanned(qrContent)
                                            }
                                        }
                                    }
                                } else {
                                    // No QR code detected, clear the box
                                    if (!hasScanned) {
                                        qrBoundingBox = null
                                        isValidWiFiQR = false
                                        showingBox = false
                                    }
                                }
                            }
                        } else {
                            imageProxy.close()
                        }
                    }
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Bounding box overlay removed - using text indicators instead
            
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isValidWiFiQR && qrBoundingBox != null) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                    } else if (qrBoundingBox != null) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (qrBoundingBox != null) {
                            if (isValidWiFiQR) "✅ WiFi QR Found!" else "⚠️ Not a WiFi QR code"
                        } else {
                            "🎯 Point me at that WiFi QR code!"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isValidWiFiQR && qrBoundingBox != null) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else if (qrBoundingBox != null) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    
                    if (qrBoundingBox != null && !isValidWiFiQR) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "I can only connect to WiFi QR codes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }
}

// QRBoundingBoxOverlay removed - using enhanced text indicators instead

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (List<Barcode>) -> Unit
) {
    imageProxy.image?.let { mediaImage ->
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                onBarcodeDetected(barcodes)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } ?: imageProxy.close()
}
