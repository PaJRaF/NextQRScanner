package com.nextqr.scanner.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextqr.scanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onOpenDetail: (Long) -> Unit,
    onOpenPremium: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    Box(Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(
                torchOn = uiState.torchOn,
                onBarcode = viewModel::onBarcodeDetected,
                modifier = Modifier.fillMaxSize(),
            )
            ScannerOverlay(
                torchOn = uiState.torchOn,
                batchMode = uiState.batchMode,
                onToggleTorch = viewModel::toggleTorch,
                onToggleBatch = viewModel::toggleBatchMode,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CameraPermissionPrompt(
                onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        uiState.lastResult?.let { result ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::dismissResult,
                sheetState = sheetState,
            ) {
                ScanResultSheet(
                    result = result,
                    onOpenDetail = { result.savedId?.let(onOpenDetail) },
                    onDismiss = viewModel::dismissResult,
                )
            }
        }
    }
}

@Composable
private fun ScannerOverlay(
    torchOn: Boolean,
    batchMode: Boolean,
    onToggleTorch: () -> Unit,
    onToggleBatch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledIconToggleButton(checked = torchOn, onCheckedChange = { onToggleTorch() }) {
                Icon(
                    if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = stringResource(R.string.action_toggle_flash),
                )
            }
            FilledIconToggleButton(checked = batchMode, onCheckedChange = { onToggleBatch() }) {
                Icon(
                    Icons.Filled.ViewList,
                    contentDescription = stringResource(R.string.action_batch_mode),
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionPrompt(
    onRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
        Text(stringResource(R.string.permission_camera_rationale))
        Button(onClick = onRequest) { Text(stringResource(R.string.permission_grant)) }
    }
}
