package no.nordicsemi.andorid.ble.test.client.data

enum class TestItem(val item: String) {
    SCANNING_FOR_SERVER("Scanning for Server"),
    CONNECTED_WITH_SERVER("Connected with Server"),
    SERVICE_DISCOVERY("Service Discovery"),
    WRITE_CHARACTERISTICS("Write Characteristics"),
    SET_INDICATION_CALLBACK("Set Indication Callback"),
    SET_NOTIFICATION_CALLBACK("Set Notification Callback"),
    ENABLE_INDICATION("Enable Indication"),
    ENABLE_NOTIFICATION("Enable Notification"),
}