package no.nordicsemi.andorid.ble.test.server.tasks

import no.nordicsemi.andorid.ble.test.server.tests.*

class Tasks {
    val tasks = listOf(
        TestSetWriteCallback(),
        TestWaitIndicationsEnabled(),
        TestSendIndication(),
        TestWaitNotificationEnabled(),
        TestSendNotification(),
        TestWriteWithMtuMerger(),
        TestWriteWithFlagMerger(),
        TestWriteWithHeaderMerger(),
        TestReliableWrite(),
        TestReadCharacteristics(),
    )
}