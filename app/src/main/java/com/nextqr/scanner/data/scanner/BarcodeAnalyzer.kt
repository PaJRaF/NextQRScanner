package com.nextqr.scanner.data.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.nextqr.scanner.domain.model.BarcodeType

/** A single decoded barcode delivered by the analyzer. */
data class DetectedBarcode(val rawValue: String, val type: BarcodeType)

/**
 * CameraX [ImageAnalysis.Analyzer] that runs ML Kit fully on-device. The camera
 * image is analysed locally and never leaves the device or touches the network.
 *
 * @param onDetected invoked on the analyzer thread for each frame that yields at
 *   least one barcode; the caller is responsible for debouncing/throttling.
 */
class BarcodeAnalyzer(
    private val onDetected: (List<DetectedBarcode>) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                SUPPORTED_ML_KIT_FORMATS.first(),
                *SUPPORTED_ML_KIT_FORMATS.drop(1).toIntArray(),
            )
            .build(),
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val detected = barcodes.mapNotNull { barcode ->
                    barcode.rawValue?.let {
                        DetectedBarcode(it, barcode.format.toBarcodeType())
                    }
                }
                if (detected.isNotEmpty()) onDetected(detected)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    fun close() = scanner.close()
}
