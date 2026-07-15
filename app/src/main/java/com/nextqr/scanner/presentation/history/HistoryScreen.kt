package com.nextqr.scanner.presentation.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextqr.scanner.R
import com.nextqr.scanner.domain.model.ScanResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpenDetail: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_history)) },
                actions = {
                    IconButton(onClick = viewModel::onFavoritesToggle) {
                        Icon(
                            if (filter.favoritesOnly) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = stringResource(R.string.history_favorites),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = filter.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(R.string.history_search)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
            )
            LazyColumn(Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { item ->
                    HistoryRow(
                        item = item,
                        onClick = { onOpenDetail(item.id) },
                        onFavorite = { viewModel.toggleFavorite(item) },
                        onDelete = { viewModel.delete(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: ScanResult,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(item.rawValue, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = { Text(item.contentType.name) },
        trailingContent = {
            androidx.compose.foundation.layout.Row {
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                }
            }
        },
    )
}
