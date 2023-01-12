package no.nordicsemi.andorid.ble.test.client.task

import no.nordicsemi.andorid.ble.test.client.tests.*

class Tasks{
    val tasks = listOf(
        TestWrite(),
        TestEnableIndication(),
        TestIndication(),
        TestEnableNotification(),
        TestSetNotification(),
        TestWriteWithDefaultSplitter(),
        TestWriteWithFlagBasedSplitter(),
        TestWriteWithHeaderBasedSplitter(),
        TestReliableWrite(),
        TestReadCharacteristics(),
    )
}