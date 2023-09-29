package no.nordicsemi.andorid.ble.test.spec

import java.util.*

object Characteristics {
    /** A random UUID. */
    val UUID_SERVICE_DEVICE: UUID by lazy { UUID.fromString("359ccc36-6fea-11ed-a1eb-0242ac120002") }
    val WRITE_CHARACTERISTIC: UUID by lazy { UUID.fromString("359ccc37-6fea-11ed-a1eb-0242ac120002") }
    val IND_CHARACTERISTIC: UUID by lazy { UUID.fromString("359ccc38-6fea-11ed-a1eb-0242ac120002") }
    val REL_WRITE_CHARACTERISTIC: UUID by lazy { UUID.fromString("359ccc39-6fea-11ed-a1eb-0242ac120002") }
    val READ_CHARACTERISTIC: UUID by lazy { UUID.fromString("359ccc40-6fea-11ed-a1eb-0242ac120002") }
}