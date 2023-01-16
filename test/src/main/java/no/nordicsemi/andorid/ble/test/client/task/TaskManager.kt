package no.nordicsemi.andorid.ble.test.client.task

import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.server.data.TestCase

interface TaskManager {
    suspend fun start(
        clientConnection: ClientConnection
    )

    fun onTaskCompleted(): TestCase
    fun onTaskFailed(): TestCase
}