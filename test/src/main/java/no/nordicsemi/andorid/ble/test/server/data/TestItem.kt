package no.nordicsemi.andorid.ble.test.server.data

enum class TestItem(val item: String) {
    START_ADVERTISING("Start Advertising"),
    SERVER_READY("Service Ready"),
    SERVICE_DISCOVERY("Service Discovery"),
    DEVICE_CONNECTION("Device Connected"),
    DEVICE_DISCONNECTION("Device Disconnected"),
    WRITE_CALLBACK("Write Callback"),
    SEND_NOTIFICATION("Send Notification"),
    WAIT_UNTIL_NOTIFICATION_ENABLED("Wait Until Notification Enabled"),
    SEND_INDICATION("Send Indication"),
    WAIT_UNTIL_INDICATION_ENABLED("Wait Until Indication Enabled"),
}


data class TestEvent(
    val testName: String,
    val isPassed: Boolean
)