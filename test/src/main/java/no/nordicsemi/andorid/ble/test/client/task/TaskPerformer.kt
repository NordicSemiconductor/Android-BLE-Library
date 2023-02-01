package no.nordicsemi.andorid.ble.test.client.task

import kotlinx.coroutines.flow.MutableStateFlow
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TaskPerformer(
    private val clientConnection: ClientConnection
) {
    val testCases = MutableStateFlow<List<TestCase>>(emptyList())
    private val tasks = Tasks().tasks

    suspend fun startTasks() {
        tasks.forEach {
            try {
                it.start(clientConnection)
                testCases.value = testCases.value + listOf(it.onTaskCompleted())
            } catch (e: Exception) {
                testCases.value = testCases.value + listOf(it.onTaskFailed())
            }
        }
    }
}