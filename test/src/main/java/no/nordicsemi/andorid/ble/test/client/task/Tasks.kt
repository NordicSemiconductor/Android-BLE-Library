package no.nordicsemi.andorid.ble.test.client.task

import no.nordicsemi.andorid.ble.test.client.tests.*

class Tasks{
    val tasks = listOf(
        TestWrite(),
        TestEnableNotification(),
        TestWaitForNotification(),
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