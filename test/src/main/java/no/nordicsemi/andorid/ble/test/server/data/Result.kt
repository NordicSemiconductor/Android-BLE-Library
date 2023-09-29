package no.nordicsemi.andorid.ble.test.server.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Result(
    val name: String,
    val isPassed: Boolean,
    val icon: ImageVector,
    val color: Color
)