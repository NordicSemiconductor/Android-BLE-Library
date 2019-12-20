package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BleServerManagerCallbacks {

	/**
	 * Called when the server was started and all services have been added successfully.
	 */
	void onServerReady();

	/**
	 * This methods is called whenever a Bluetooth LE device connects or when was already connected
	 * when the {@link BleServerManager} was created.
	 *
	 * @param device the connected device.
	 */
	void onDeviceConnectedToServer(@NonNull final BluetoothDevice device);

	/**
	 * Called when the device has disconnected (when the callback returned
	 * {@link android.bluetooth.BluetoothGattServerCallback#onConnectionStateChange(BluetoothDevice, int, int)}
	 * with state DISCONNECTED).
	 *
	 * @param device the device that got disconnected.
	 */
	void onDeviceDisconnectedFromServer(@NonNull final BluetoothDevice device);
}
