package no.nordicsemi.andorid.ble.test.server.tasks

interface TaskManager {
    /**
     * Starts the task.
     */
    suspend fun start()

    /**
     * Returns the name of the task.
     */
    fun taskName(): String
}