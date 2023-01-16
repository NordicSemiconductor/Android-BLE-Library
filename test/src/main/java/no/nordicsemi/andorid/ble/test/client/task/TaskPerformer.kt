package no.nordicsemi.andorid.ble.test.client.task

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.data.TestCase

class TaskPerformer(
    private val clientConnection: ClientConnection
) {
    val testCases = mutableListOf<List<TestCase>>(emptyList())
    private val tasks = Tasks().tasks

    suspend fun startTasks() {
        tasks.forEach {
            try {
                it.start(clientConnection)
                testCases.add(listOf(it.onTaskCompleted()))
            } catch (e: Exception) {
                testCases.add(listOf(it.onTaskFailed()))
            }
        }
    }
}