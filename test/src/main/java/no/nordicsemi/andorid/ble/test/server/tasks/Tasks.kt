package no.nordicsemi.andorid.ble.test.server.tasks

import no.nordicsemi.andorid.ble.test.server.tests.*

class Tasks {
    val tasks = listOf(
        TestSetWriteCallback(),
        TestWaitNotificationEnabled(),
        TestSendNotification(),
        TestWaitIndicationsEnabled(),
        TestSendIndication(),
        TestWriteWithMtuMerger(),
        TestWriteWithFlagMerger(),
        TestWriteWithHeaderMerger(),
        TestSetReadCharacteristics(),
        TestBeginAtomicRequestQueue(),
        TestReliableWrite(),
    )
}