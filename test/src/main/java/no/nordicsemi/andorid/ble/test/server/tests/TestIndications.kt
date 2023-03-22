package no.nordicsemi.andorid.ble.test.server.tests

import no.nordicsemi.andorid.ble.test.server.tasks.TaskManager
import no.nordicsemi.andorid.ble.test.server.repository.ServerConnection
import no.nordicsemi.andorid.ble.test.spec.Callbacks.SEND_INDICATION
import no.nordicsemi.andorid.ble.test.spec.Callbacks.WAIT_UNTIL_INDICATION_ENABLED
import no.nordicsemi.andorid.ble.test.spec.FlagBasedPacketSplitter
import no.nordicsemi.andorid.ble.test.spec.Requests
import no.nordicsemi.android.ble.WriteRequest
import no.nordicsemi.android.ble.ktx.suspend

class TestIndications(
    private val serverConnection: ServerConnection,
) : TaskManager {
    /**
     * Waits until the indication is enabled and sends an Indication response. It utilizes the [WriteRequest.split] callback
     * to chunk the data into multiple packets, if the data cannot be sent in a single write operation.
     */
    override suspend fun start() {
        serverConnection.testWaiUntilIndicationEnabled(Requests.readRequestInTrigger)
        serverConnection.testSendIndication(Requests.indicationRequest)
            .split(FlagBasedPacketSplitter())
            .suspend()
    }

    override fun taskName(): String {
        val indications = listOf(WAIT_UNTIL_INDICATION_ENABLED, SEND_INDICATION)
        return indications.joinToString(separator = "\n")
    }
}