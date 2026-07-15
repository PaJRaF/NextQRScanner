package com.nextqr.scanner.presentation.scanner

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nextqr.scanner.data.scanner.BarcodeAnalyzer
import java.util.concurrent.Executors

/**
 * CameraX preview bound to the composition lifecycle. Frames are analysed
 * on-device by [BarcodeAnalyzer]; nothing leaves the device.
 *
 * @param torchOn drives the flash/torch.
 * @param onBarcode called (main-safe not guaranteed — hop threads in caller) for
 *   each decoded frame.
 */
@Composable
fun CameraPreview(
    torchOn: Boolean,
    onBarcode: (rawValue: String, type: com.nextqr.scanner.domain.model.BarcodeType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val analyzer = remember {
        BarcodeAnalyzer { detected ->
            detected.firstOrNull()?.let { onBarcode(it.rawValue, it.type) }
        }
    }

    DisposableEffect(lifecycleOwner, torchOn) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var boundCamera: androidx.camera.core.Camera? = null

        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                    ),
                )
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(analysisExecutor, analyzer) }

            cameraProvider.unbindAll()
            boundCamera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
            boundCamera?.cameraControl?.enableTorch(torchOn)
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}
