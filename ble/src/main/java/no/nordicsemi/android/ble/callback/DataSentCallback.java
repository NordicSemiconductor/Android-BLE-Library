package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataSplitter;

public interface DataSentCallback {

	/**
	 * Callback received each time the value was written to a characteristic or descriptor.
	 *
	 * @param device target device.
	 * @param data the data sent. If the {@link DataSplitter} was used, this contains the full data.
	 */
	void onDataSent(final @NonNull BluetoothDevice device, final @NonNull Data data);
}
