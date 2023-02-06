package no.nordicsemi.andorid.ble.test.server.tasks

interface TaskManager {
    suspend fun start()
    fun taskName(): String
}