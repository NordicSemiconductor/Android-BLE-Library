package no.nordicsemi.android.ble.example.game.spec

import java.util.*

class DeviceSpecifications {

    companion object{
        /** A random UUID. */
        val UUID_SERVICE_DEVICE: UUID by lazy { UUID.fromString("fe72265a-0f16-4b45-b6b7-95889930140a") }
        val UUID_MSG_CHARACTERISTIC: UUID by lazy { UUID.fromString("fe72265b-0f16-4b45-b6b7-95889930140a") }
    }
}