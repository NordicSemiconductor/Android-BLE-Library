package no.nordicsemi.android.ble.callback;

public interface FailCallback {

	/**
	 * A callback invoked when the request has failed with status other than {@link android.bluetooth.BluetoothGatt#GATT_SUCCESS}.
	 */
	void onRequestFailed(final int status);
}
