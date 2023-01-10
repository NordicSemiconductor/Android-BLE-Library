package no.nordicsemi.andorid.ble.test.client.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.ble.ktx.state.ConnectionState

data class Result(
    val name: String,
    val isPassed: Boolean,
    val icon: ImageVector,
    val color: Color
)

data class ClientViewState(
    val state: ConnectionState = ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.UNKNOWN),
    val testItems: List<TestCase> = emptyList()
) {
    val resultList: List<Result> = getItem(testItems)

    private fun getItem(a: List<TestCase>): List<Result> = a.map {
        val (icon, color) = if (it.isPassed) {
            Icons.Default.Check to Color.Green
        } else {
            Icons.Default.Close to Color.Red
        }
        Result(it.testName, it.isPassed, icon, color)
    }
}