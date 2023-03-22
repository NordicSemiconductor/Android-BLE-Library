package no.nordicsemi.andorid.ble.test.client.task

import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.andorid.ble.test.client.repository.ClientConnection
import no.nordicsemi.andorid.ble.test.client.tests.*
import javax.inject.Inject

@ViewModelScoped
class ClientTask @Inject constructor (
    clientConnection: ClientConnection,
){
    val tasks = listOf(
        TestWrite(clientConnection),
        TestNotification(clientConnection),
        TestIndication(clientConnection),
        TestWriteWithDefaultSplitter(clientConnection),
        TestWriteWithFlagBasedSplitter(clientConnection),
        TestWriteWithHeaderBasedSplitter(clientConnection),
        TestReadCharacteristics(clientConnection),
        TestBeginAtomicRequestQueue(clientConnection),
        TestReliableWrite(clientConnection),
    )
}