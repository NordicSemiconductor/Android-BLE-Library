package no.nordicsemi.andorid.ble.test.server.tasks

import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection

class TaskPerformer(
    private val serverConnection: ServerConnection
) {
    val testCases = MutableStateFlow(emptyList<TestCase>())
    private val tasks = Tasks().tasks

    suspend fun startTasks() {
        tasks.forEach {
            try {
                it.start(serverConnection)
                testCases.value = testCases.value + listOf(it.onTaskCompleted())
            } catch (e: Exception) {
                testCases.value = testCases.value + listOf(it.onTaskFailed())
            }
        }
    }
}