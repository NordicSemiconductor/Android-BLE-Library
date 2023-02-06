package no.nordicsemi.andorid.ble.test.server.tasks

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import javax.inject.Inject

@ViewModelScoped
class TaskPerformer @Inject constructor(
    serverTask: ServerTask,
) {
    private val _testCases = MutableStateFlow(emptyList<TestCase>())
    val testCases = _testCases.asStateFlow()
    private val tasks = serverTask.tasks

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