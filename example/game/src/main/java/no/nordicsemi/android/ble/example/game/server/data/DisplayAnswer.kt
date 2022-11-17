package no.nordicsemi.android.ble.example.game.server.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class DisplayAnswer(
    val id: Int,
    val text: String,
    val isSelected: Boolean,
    val enableSelection: Boolean,
    val color: ColorState
)

enum class ColorState(val color: @Composable () -> Color) {
    CORRECT({ Color.Green }),
    SELECTED_AND_TIMER_RUNNING({ MaterialTheme.colorScheme.secondary }),
    NOT_SELECTED_AND_TIMER_RUNNING({ Color.Unspecified }),
    NONE({ Color.Red })
}
