package no.nordicsemi.andorid.ble.test.server.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import no.nordicsemi.andorid.ble.test.client.viewmodel.Result
import no.nordicsemi.andorid.ble.test.server.data.TestCase

data class ServerViewState(
    val state: TestState = WaitingForClient(0),
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