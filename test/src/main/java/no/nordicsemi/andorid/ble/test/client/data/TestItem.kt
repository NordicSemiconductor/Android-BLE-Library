package no.nordicsemi.andorid.ble.test.client.data

enum class TestItem(val item: String) {
    SCANNING_FOR_SERVER("Scanning for Server"),
    CONNECTED_WITH_SERVER("Connected with Server"),
    SERVICE_DISCOVERY("Service Discovery"),
    WRITE_CHARACTERISTICS("Write Characteristics"),
    RELIABLE_WRITE("Begin Reliable Write"),
    ATOMIC_REQUEST_QUEUE("Begin Atomic Request Queue"),
    FLAG_BASED_SPLITTER("Write with flag based splitter"),
    DEFAULT_MTU_SPLITTER("Write with default mtu size splitter"),
    HEADER_BASED_SPLITTER("Write with header based splitter"),
    SET_INDICATION("Set Indication Callback"),
    SET_NOTIFICATION("Set Notification Callback"),
    ENABLE_INDICATION("Enable Indication"),
    ENABLE_NOTIFICATION("Enable Notification"),
}