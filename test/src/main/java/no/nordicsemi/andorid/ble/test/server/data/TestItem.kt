package no.nordicsemi.andorid.ble.test.server.data

enum class TestItem(val item: String) {
    START_ADVERTISING("Start Advertising"),
    SERVER_READY("Service Ready"),
    SERVICE_DISCOVERY("Service Discovery"),
    DEVICE_CONNECTION("Device Connected"),
    DEVICE_DISCONNECTION("Device Disconnected"),
    WRITE_CALLBACK("Write Callback"),
    WRITE_WITH_MTU_SIZE_MERGER("Write with mtu size merger"),
    WRITE_WITH_FLAG_BASED_MERGER("Write with flag based merger"),
    WRITE_WITH_HEADER_BASED_MERGER("Write with header based merger"),
    SEND_NOTIFICATION("Send Notification"),
    WAIT_UNTIL_NOTIFICATION_ENABLED("Wait Until Notification Enabled"),
    SEND_INDICATION("Send Indication"),
    WAIT_UNTIL_INDICATION_ENABLED("Wait Until Indication Enabled"),
}


data class TestEvent(
    val testName: String,
    val isPassed: Boolean
)