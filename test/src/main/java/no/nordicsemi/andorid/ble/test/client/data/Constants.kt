package no.nordicsemi.andorid.ble.test.client.data

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
    autem omnis ut consequatur inventore. Cum consequatur consequatur et laudantium dolorem et enim odit.
    """.toByteArray()
val reliableRequest = """
    Lorem ipsum dolor sit amet. Qui quia nihil ad mollitia animi qui tenetur labore est amet quia sed 
    sunt doloremque! Sit perspiciatis voluptatum sit consequuntur dicta ut odio dignissimos hic dicta 
    labore et nihil internos non amet eveniet sed laudantium aperiam? Vel eaque odit aut labore 
    illo eos quia voluptatibus ad mollitia necessitatibus? Et fugit dolor aut provident obcaecati qui 
    delectus quia eum reprehenderit sunt aut facere voluptas non facere eveniet sed omnis autem. At 
    reprehenderit optio qui consequuntur consequatur qui natus aspernatur et voluptatem cupiditate non 
    quibusdam ipsum in recusandae aperiam At sapiente mollitia. Aut facilis quia in placeat 
    adipisci ex quis laudantium sed repellendus aliquam? Qui incidunt dolorem et architecto temporibus 
    in impedit praesentium. Est rerum fugiat aut nesciunt obcaecati est quos possimus est ducimus eveniet 
    est molestiae sapiente. Et molestias minus eum aliquam obcaecati aut numquam ullam.
    """.toByteArray()

val request = "This is a write request".toByteArray()

const val SCANNING_FOR_SERVER = "Scanning for Server"
const val CONNECTED_WITH_SERVER = "Connected with Server"
const val SERVICE_DISCOVERY = "Service Discovery"
const val WRITE_CHARACTERISTICS = "Write Characteristics"
const val RELIABLE_WRITE = "Begin Reliable Write"
const val ATOMIC_REQUEST_QUEUE = "Begin Atomic Request Queue"
const val FLAG_BASED_SPLITTER = "Write with flag based splitter"
const val DEFAULT_MTU_SPLITTER = "Write with default mtu size splitter"
const val HEADER_BASED_SPLITTER = "Write with header based splitter"
const val SET_INDICATION = "Set Indication Callback"
const val SET_NOTIFICATION = "Set Notification Callback"
const val ENABLE_INDICATION = "Enable Indication"
const val ENABLE_NOTIFICATION = "Enable Notification"