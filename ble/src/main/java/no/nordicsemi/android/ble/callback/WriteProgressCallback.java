package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.data.DataSplitter;

public interface WriteProgressCallback {

	/**
	 * Callback called each time a packet has been sent when {@link DataSplitter} was used.
	 *
	 * @param device target device.
	 * @param data the last packet sent.
	 * @param index the index of a packet that the initial Data was cut into.
	 */
	void onPacketSent(final @NonNull BluetoothDevice device, final byte[] data, final int index);
}
