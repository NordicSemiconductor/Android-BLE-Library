package no.nordicsemi.andorid.ble.test.client.task

import no.nordicsemi.andorid.ble.test.client.tests.*

class Tasks{
    val tasks = listOf(
        TestWrite(),
        TestSetNotification(),
        TestEnableNotification(),
        TestWaitForNotification(),
        TestSetIndication(),
        TestEnableIndication(),
        TestWaitForIndication(),
        TestWriteWithDefaultSplitter(),
        TestWriteWithFlagBasedSplitter(),
        TestWriteWithHeaderBasedSplitter(),
        TestReadCharacteristics(),
        TestBeginAtomicRequestQueue(),
        TestReliableWrite(),
    )
}