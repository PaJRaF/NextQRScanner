package com.nextqr.scanner.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextqr.scanner.R
import com.nextqr.scanner.util.ContentActions
import com.nextqr.scanner.util.shareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    onBack: () -> Unit,
    viewModel: ScanDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (uiState.result?.isFavorite == true) {
                                Icons.Filled.Star
                            } else {
                                Icons.Filled.StarBorder
                            },
                            contentDescription = null,
                        )
                    }
                    IconButton(onClick = {
                        uiState.result?.rawValue?.let { shareText(context, it) }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        val result = uiState.result
        val parsed = uiState.parsed
        if (result == null || parsed == null) return@Scaffold

        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(parsed.type.name, style = MaterialTheme.typography.labelLarge)
            Text(result.rawValue, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.detail_type, result.barcodeType.name),
                style = MaterialTheme.typography.labelLarge,
            )
            Button(
                onClick = { ContentActions.perform(context, parsed, allowDangerous = false) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_open))
            }
        }
    }
}
