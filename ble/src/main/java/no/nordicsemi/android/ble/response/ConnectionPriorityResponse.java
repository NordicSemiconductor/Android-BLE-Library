package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.ConnectionPriorityCallback;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ConnectionPriorityResponse implements ConnectionPriorityCallback {
	private BluetoothDevice mDevice;
	private int mInterval;
	private int mLatency;
	private int mSupervisionTimeout;

	@Override
	public void onConnectionUpdated(@NonNull final BluetoothDevice device, final int interval, final int latency, final int timeout) {
		mDevice = device;
		mInterval = interval;
		mLatency = latency;
		mSupervisionTimeout = timeout;
	}

	@NonNull
	public BluetoothDevice getBluetoothDevice() {
		return mDevice;
	}

	public int getInterval() {
		return mInterval;
	}

	public int getLatency() {
		return mLatency;
	}

	public int getSupervisionTimeout() {
		return mSupervisionTimeout;
	}
}
