package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataMerger;

public interface ReadProgressCallback {

	/**
	 * Callback received each time the value was read or has changed using notifications or indications
	 * when {@link DataMerger} was used.
	 *
	 * @param device target device.
	 * @param data the last packet received.
	 * @param index the index of a packet that will be merged into a single Data.
	 */
	void onPacketReceived(final @NonNull BluetoothDevice device, final byte[] data, final int index);
}
