package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.MtuCallback;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MtuResult implements MtuCallback {
	private BluetoothDevice mDevice;
	private int mMtu;

	@Override
	public void onMtuChanged(final @NonNull BluetoothDevice device, final int mtu) {
		mDevice = device;
		mMtu = mtu;
	}

	@NonNull
	public BluetoothDevice getBluetoothDevice() {
		return mDevice;
	}

	/**
	 * Returns the agreed MTU. The maximum packet size is 3 bytes less then MTU.
	 * @return the MTU.
	 */
	public int getMtu() {
		return mMtu;
	}
}
