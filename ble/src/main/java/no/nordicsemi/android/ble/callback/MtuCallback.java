package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

public interface MtuCallback {

	/**
	 * Method called when the MTU request has finished with success. The MTU value may
	 * be different than requested one. The maximum packet size is 3 bytes less then MTU.
	 *
	 * @param device target device.
	 * @param mtu the new MTU (Maximum Transfer Unit).
	 */
	void onMtuChanged(final @NonNull BluetoothDevice device, final int mtu);
}
