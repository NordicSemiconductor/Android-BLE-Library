package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;

public interface SuccessCallback {

	/**
	 * A callback invoked when the request completed successfully.
	 *
	 * @param device target device.
	 */
	void onRequestCompleted(final BluetoothDevice device);
}
