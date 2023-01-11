package no.nordicsemi.andorid.ble.test.server.data

const val START_ADVERTISING = "Start Advertising"
const val SERVER_READY = "Service Ready"
const val SERVICE_DISCOVERY = "Service Discovery"
const val DEVICE_CONNECTION = "Device Connected"
const val DEVICE_DISCONNECTION = "Device Disconnected"
const val WRITE_CALLBACK = "Write Callback"
const val RELIABLE_WRITE = "Reliable Write"
const val MTU_SIZE_MERGER = "Write with mtu size merger"
const val FLAG_BASED_MERGER = "Write with flag based merger"
const val HEADER_BASED_MERGER = "Write with header based merger"
const val SEND_NOTIFICATION = "Send Notification"
const val WAIT_UNTIL_NOTIFICATION_ENABLED = "Wait Until Notification Enabled"
const val SEND_INDICATION = "Send Indication"
const val WAIT_UNTIL_INDICATION_ENABLED = "Wait Until Indication Enabled"

val indicationRequest = "This is Indication".toByteArray()
val notificationRequest = "This is Notification".toByteArray()

const val FULL = 0b11.toByte()
const val BEGIN = 0b00.toByte()
const val CONTINUATION = 0b01.toByte()
const val END = 0b10.toByte()