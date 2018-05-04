package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Generic read response class that returns the data received and the device from which data
 * were read.
 * Overriding class must call super on {@link #onDataReceived(BluetoothDevice, Data)} in
 * order to make getters work properly.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ReadResponse implements DataReceivedCallback {
	private BluetoothDevice mDevice;
	private Data mData;

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		mDevice = device;
		mData = data;
	}

	@NonNull
	public BluetoothDevice getBluetoothDevice() {
		return mDevice;
	}

	@NonNull
	public Data getRawData() {
		return mData;
	}
}
