package no.nordicsemi.andorid.ble.test.server.tasks

import no.nordicsemi.andorid.ble.test.server.tests.*

class Tasks {
    val tasks = listOf(
        TestSetWriteCallback(),
        TesWaitIndicationsEnabled(),
        TestSendIndication(),
        TestWaitNotificationEnabled(),
        TestSendNotification(),
        TestWriteWithMtuMerger(),
        TestWriteWithFlagMerger(),
        TestWriteWithHeaderMerger(),
    )
}