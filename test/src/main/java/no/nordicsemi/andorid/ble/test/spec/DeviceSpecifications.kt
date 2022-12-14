package no.nordicsemi.andorid.ble.test.spec

import java.util.*

class DeviceSpecifications {
    companion object{
        /** A random UUID. */
        val UUID_SERVICE_DEVICE: UUID by lazy { UUID.fromString("359ccc36-6fea-11ed-a1eb-0242ac120002") }
        val WRITE_CHARACTERISTIC: UUID by lazy { UUID.fromString("359ccc37-6fea-11ed-a1eb-0242ac120002") }
    }
}