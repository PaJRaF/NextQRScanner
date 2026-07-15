package com.nextqr.scanner.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nextqr.scanner.R
import com.nextqr.scanner.domain.model.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenPremium: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_go_premium)) },
                modifier = Modifier.clickable(onClick = onOpenPremium),
            )
            HorizontalDivider()

            SwitchRow(
                title = stringResource(R.string.settings_dynamic_color),
                checked = settings.useDynamicColor,
                onCheckedChange = viewModel::setDynamicColor,
            )
            ThemeRow(
                current = settings.theme,
                onSelect = viewModel::setTheme,
            )
            HorizontalDivider()

            SwitchRow(
                title = stringResource(R.string.settings_vibrate),
                checked = settings.vibrateOnScan,
                onCheckedChange = viewModel::setVibrate,
            )
            SwitchRow(
                title = stringResource(R.string.settings_sound),
                checked = settings.soundOnScan,
                onCheckedChange = viewModel::setSound,
            )
            HorizontalDivider()

            SwitchRow(
                title = stringResource(R.string.settings_url_safety),
                subtitle = stringResource(R.string.settings_url_safety_sub),
                checked = settings.checkUrlSafety,
                onCheckedChange = viewModel::setCheckUrlSafety,
            )
            SwitchRow(
                title = stringResource(R.string.settings_history_enabled),
                checked = settings.historyEnabled,
                onCheckedChange = viewModel::setHistoryEnabled,
            )
            SwitchRow(
                title = stringResource(R.string.settings_ad_consent),
                subtitle = stringResource(R.string.settings_ad_consent_sub),
                checked = settings.adConsentGranted,
                onCheckedChange = viewModel::setAdConsent,
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_delete_data)) },
                supportingContent = { Text(stringResource(R.string.settings_delete_data_sub)) },
                modifier = Modifier.clickable { viewModel.deleteAllData() },
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
    )
}

@Composable
private fun ThemeRow(current: ThemePreference, onSelect: (ThemePreference) -> Unit) {
    Text(
        text = stringResource(R.string.settings_theme),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ThemePreference.entries.forEach { pref ->
            FilterChip(
                selected = current == pref,
                onClick = { onSelect(pref) },
                label = { Text(pref.name) },
            )
        }
    }
}
