package no.nordicsemi.andorid.ble.test.server.tasks

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection

class TaskPerformer(
    private val scope: CoroutineScope,
    private val serverConnection: ServerConnection
) {
    val testCases = mutableListOf<List<TestCase>>(emptyList())
    private val tasks = Tasks().tasks

    suspend fun startTasks() {
        tasks.forEach {
            try {
                it.start(scope, serverConnection)
                testCases.add(listOf(it.onTaskCompleted()))
            } catch (e: Exception) {
                testCases.add(listOf(it.onTaskFailed()))
            }
        }
    }
}