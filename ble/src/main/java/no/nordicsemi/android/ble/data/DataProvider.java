package no.nordicsemi.android.ble.data;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DataProvider {

    /**
     * Returns the data that should be returned as Read response.
     * <p>
     * The data can be longer than MTU-1, but must not be longer than 512 bytes:
     * <pre>
     * Bluetooth LE Core Specification, version 5.2, Vol 3, Part F
     * Chapter 3.2.9: Long attribute values
     * "The maximum length of an attribute value shall be 512 octets."
     * </pre>
     *
     * @param device the target device.
     * @return the data to be sent.
     */
    @Nullable
    byte[] getData(@NonNull final BluetoothDevice device);
}
