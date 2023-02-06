package no.nordicsemi.andorid.ble.test.server.tasks

import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.server.tests.*
import javax.inject.Inject

@ViewModelScoped
class ServerTask @Inject constructor(
    serverConnection: ServerConnection
) {
    val tasks = listOf(
        TestSetWriteCallback(serverConnection),
        TestNotification(serverConnection),
        TestIndications(serverConnection),
        TestWriteWithMtuMerger(serverConnection),
        TestWriteWithFlagMerger(serverConnection),
        TestWriteWithHeaderMerger(serverConnection),
        TestSetReadCharacteristics(serverConnection),
        TestBeginAtomicRequestQueue(serverConnection),
        TestReliableWrite(serverConnection),
    )
}