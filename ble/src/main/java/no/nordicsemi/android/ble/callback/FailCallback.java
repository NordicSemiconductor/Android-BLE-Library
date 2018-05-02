package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;

public interface FailCallback {
	int REASON_REQUEST_FAILED = -1;
	int REASON_NULL_ATTRIBUTE = -2;

	/**
	 * A callback invoked when the request has failed with status other than
	 * {@link android.bluetooth.BluetoothGatt#GATT_SUCCESS}.
	 *
	 * @param device target device.
	 * @param status error status code, one of BluetoothGatt#GATT_* constants or
	 *               {@link #REASON_REQUEST_FAILED}.
	 */
	void onRequestFailed(final BluetoothDevice device, final int status);
}
