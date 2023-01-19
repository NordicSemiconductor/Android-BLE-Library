package no.nordicsemi.andorid.ble.test.client.task

import no.nordicsemi.andorid.ble.test.client.tests.*

class Tasks{
    val tasks = listOf(
        TestWrite(),
        TestSetIndication(),
        TestEnableIndication(),
        TestWaitForIndication(),
        TestSetNotification(),
        TestEnableNotification(),
        TestWaitForNotification(),
        TestRemoveNotificationCallback(),
        TestWriteWithDefaultSplitter(),
        TestWriteWithFlagBasedSplitter(),
        TestWriteWithHeaderBasedSplitter(),
        TestReliableWrite(),
        TestBeginAtomicRequestQueue(),
    )
}