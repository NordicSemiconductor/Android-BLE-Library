package no.nordicsemi.andorid.ble.test.spec

object  Requests {
    val splitterRequest = """
    Lorem ipsum dolor sit amet. Ab vitae odio eos veniam exercitationem qui totam provident in
    earum eveniet sed suscipit libero est temporibus eius. Ut Quis deserunt sit ipsa earum cum
    esse tenetur id pariatur delectus vel sapiente exercitationem est harum dolore et accusantium
    dicta. Qui officia dolor ut provident numquam sit dolor quae sit ipsum dolores et autem rerum.
    Est maxime nihil aut beatae excepturi ut rerum explicabo.Et ullam expedita cum cupiditate
    doloremque cum omnis incidunt sed dolores maxime sed voluptatibus quisquam. Qui recusandae
    ipsam qui iste quia sit deleniti mollitia. Qui totam dolorem et ipsa dolor a architecto omnis ab
    consectetur eveniet. Ex quae laborum id doloribus tenetur non porro dolorum et assumenda nesciunt est
    nihil enim eos provident officiis. Est itaque nostrum vel accusantium reiciendis nam omnis sunt ad
    autem omnis ut consequatur inventore.
    """.toByteArray()
    val reliableRequest = "This is reliable write request.".toByteArray()
    val secondReliableRequest = "This is second reliable write request".toByteArray()
    val writeRequest = "This is a write request".toByteArray()
    val atomicRequestQueue = "This is write request in an atomic request queue".toByteArray()
    val atomicRequest = "This is read characteristics in atomic request queue".toByteArray()
    val indicationRequest = "This is Indication and this is tested to be send on packet splitter".toByteArray()
    val notificationRequest = "This is Notification request".toByteArray()
    val sendNotificationInThenCallback = "This is Notification request send in the then callback".toByteArray()
    val readRequest = "This is read request".toByteArray()
    val readRequestInTrigger =  "This is read request initiated in the trigger callback".toByteArray()
}

object Flags {
    const val FLAG_BASED_SPLITTER = "Write with flag based splitter"
    const val DEFAULT_MTU_SPLITTER = "Write with default mtu size splitter"
    const val HEADER_BASED_SPLITTER = "Write with header based splitter"

    const val MTU_SIZE_MERGER = "Write with mtu size merger"
    const val FLAG_BASED_MERGER = "Write with flag based merger"
    const val HEADER_BASED_MERGER = "Write with header based merger"

    const val FULL = 0b11.toByte()
    const val BEGIN = 0b00.toByte()
    const val CONTINUATION = 0b01.toByte()
    const val END = 0b10.toByte()
}

object Callbacks {
    const val WRITE_CHARACTERISTICS = "Write Characteristics"
    const val RELIABLE_WRITE = "Begin Reliable Write"
    const val ATOMIC_REQUEST_QUEUE = "Begin Atomic Request Queue"
    const val WRITE_CALLBACK = "Write Callback"

    const val ENABLE_INDICATION = "Enable Indication"
    const val WAIT_FOR_INDICATION_CALLBACK = "Wait for Indication"
    const val WAIT_UNTIL_INDICATION_ENABLED = "Wait Until Indication Enabled"
    const val SEND_INDICATION = "Send Indication"

    const val ENABLE_NOTIFICATION = "Enable Notification"
    const val WAIT_FOR_NOTIFICATION_CALLBACK = "Wait for Notification"
    const val WAIT_UNTIL_NOTIFICATION_ENABLED = "Wait Until Notification Enabled"
    const val SEND_NOTIFICATION = "Send Notification"

    const val READ_CHA = "Set Characteristics for read operation"
    const val READ_CHARACTERISTICS = "Read Characteristics"
}

object Connections {
    const val SCANNING_FOR_SERVER = "Scanning for Server"
    const val CONNECTED_WITH_SERVER = "Connected with Server"
    const val SERVICE_DISCOVERY = "Service Discovery"
    const val START_ADVERTISING = "Start Advertising"
    const val SERVER_READY = "Service Ready"
    const val DEVICE_CONNECTION = "Device Connected"
    const val DEVICE_DISCONNECTION = "Device Disconnected"
}