package no.nordicsemi.android.ble.ktx

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.UUID

/**
 * Returns a characteristic with a given UUID out of the list of
 * characteristics offered by this service, matching the required properties and, optionally,
 * the Instance Id.
 *
 * <p>This is a convenience function to allow access to a given characteristic
 * without enumerating over the list returned by {@link #getCharacteristics}
 * manually.
 *
 * @return GATT characteristic object or null if no characteristic was found.
 */
fun BluetoothGattService.getCharacteristic(
    uuid: UUID,
    requiredProperties: Int = 0,
    instanceId: Int? = null,
): BluetoothGattCharacteristic? = characteristics
    .firstOrNull { it.uuid == uuid && (instanceId == null || it.instanceId == instanceId) }
    ?.takeIf {
        it.properties and requiredProperties == requiredProperties
    }