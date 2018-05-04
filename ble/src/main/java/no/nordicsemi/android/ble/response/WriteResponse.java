package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Generic write response class that returns the target device and the data that were sent.
 * Overriding class must call super on {@link #onDataSent(BluetoothDevice, Data)} in
 * order to make getters work properly.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class WriteResponse implements DataSentCallback {
	private BluetoothDevice mDevice;
	private Data mData;

	@Override
	public void onDataSent(@NonNull final BluetoothDevice device, @NonNull final Data data) {
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
