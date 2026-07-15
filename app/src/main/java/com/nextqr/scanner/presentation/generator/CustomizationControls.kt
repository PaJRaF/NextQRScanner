package com.nextqr.scanner.presentation.generator

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextqr.scanner.domain.model.ErrorCorrection
import com.nextqr.scanner.domain.model.ModuleShape

/** Premium generator controls: error-correction level and module shape. */
@Composable
fun CustomizationControls(
    onErrorCorrection: (ErrorCorrection) -> Unit,
    onModuleShape: (ModuleShape) -> Unit,
    modifier: Modifier = Modifier,
) {
    var ecLevel by remember { mutableStateOf(ErrorCorrection.MEDIUM) }
    var shape by remember { mutableStateOf(ModuleShape.SQUARE) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Error correction")
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ErrorCorrection.entries.forEach { level ->
                FilterChip(
                    selected = ecLevel == level,
                    onClick = { ecLevel = level; onErrorCorrection(level) },
                    label = { Text(level.name) },
                )
            }
        }
        Text("Module shape")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModuleShape.entries.forEach { s ->
                FilterChip(
                    selected = shape == s,
                    onClick = { shape = s; onModuleShape(s) },
                    label = { Text(s.name) },
                )
            }
        }
    }
}
