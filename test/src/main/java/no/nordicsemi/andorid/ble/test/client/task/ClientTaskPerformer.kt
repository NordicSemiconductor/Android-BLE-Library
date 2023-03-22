package no.nordicsemi.andorid.ble.test.client.task

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import javax.inject.Inject

@ViewModelScoped
class ClientTaskPerformer @Inject constructor(
    clientTask: ClientTask,
) {
    private val _testCases = MutableStateFlow<List<TestCase>>(emptyList())
    val testCases = _testCases.asStateFlow()
    private val tasks = clientTask.tasks

    suspend fun startTasks() {
        tasks.forEach {
            try {
                it.start()
                _testCases.value = _testCases.value + listOf(TestCase(it.taskName(), true))
            } catch (e: Exception) {
                _testCases.value = _testCases.value + listOf(TestCase(it.taskName(), false))
            }
        }
    }
}