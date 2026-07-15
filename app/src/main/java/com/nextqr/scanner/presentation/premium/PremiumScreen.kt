package com.nextqr.scanner.presentation.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextqr.scanner.R
import com.nextqr.scanner.domain.model.PremiumProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.premium_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.premium_headline),
                style = MaterialTheme.typography.headlineLarge,
            )

            FeatureComparison()

            if (status.isPremium) {
                Text(stringResource(R.string.premium_active), style = MaterialTheme.typography.titleLarge)
            } else {
                products.forEach { product ->
                    ProductCard(product = product, onBuy = { viewModel.purchase(product) })
                }
                if (products.isEmpty()) {
                    Text(stringResource(R.string.premium_loading))
                }
            }
        }
    }
}

@Composable
private fun FeatureComparison() {
    val features = listOf(
        R.string.premium_feature_no_ads,
        R.string.premium_feature_customization,
        R.string.premium_feature_batch_export,
        R.string.premium_feature_unlimited_history,
        R.string.premium_feature_cloud_backup,
    )
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            features.forEach { res ->
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(stringResource(res))
                }
            }
        }
    }
}

@Composable
private fun ProductCard(product: PremiumProduct, onBuy: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(product.title, style = MaterialTheme.typography.titleLarge)
            Text(product.description)
            Button(onClick = onBuy, modifier = Modifier.fillMaxWidth()) {
                Text("${product.formattedPrice}")
            }
        }
    }
}
