package com.nextqr.scanner.presentation.generator

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextqr.scanner.R
import com.nextqr.scanner.util.rememberSaveQrToGallery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    onOpenPremium: () -> Unit,
    viewModel: GeneratorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val saveToGallery = rememberSaveQrToGallery()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_generate)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.content,
                onValueChange = viewModel::onContentChange,
                label = { Text(stringResource(R.string.generator_content_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )

            uiState.bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = stringResource(R.string.generator_preview_desc),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
            }

            if (!isPremium) {
                TextButton(onClick = onOpenPremium) {
                    Text(stringResource(R.string.generator_customize_premium))
                }
            } else {
                CustomizationControls(
                    onErrorCorrection = viewModel::onErrorCorrection,
                    onModuleShape = viewModel::onModuleShape,
                )
            }

            Button(
                onClick = {
                    uiState.bitmap?.let { saveToGallery(it) }
                    viewModel.saveToHistory()
                },
                enabled = uiState.bitmap != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.generator_save))
            }
        }
    }
}
