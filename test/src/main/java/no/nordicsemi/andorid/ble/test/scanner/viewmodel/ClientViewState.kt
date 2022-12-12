package no.nordicsemi.andorid.ble.test.scanner.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.andorid.ble.test.advertiser.view.TestEvent
import no.nordicsemi.android.ble.ktx.state.ConnectionState

data class ClientViewState(
    val state: ConnectionState = ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.UNKNOWN),
    val testItems: List<TestEvent> = emptyList()
) {
    val color = if (getIcon() == Icons.Default.Check) Color.Green
    else Color.Red

    fun getIcon(): ImageVector? {
        var icon: ImageVector? = null
        testItems.forEach {
            icon = when (it.isPassed) {
                true -> Icons.Default.Check
                else -> Icons.Default.Close
            }
        }
        return icon
    }
}