package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public interface MtuCallback {

	/**
	 * Method called when the MTU request has finished with success. The MTU value may
	 * be different than requested one.
	 *
	 * @param device target device.
	 * @param mtu the new MTU (Maximum Transfer Unit).
	 */
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	void onMtuChanged(final @NonNull BluetoothDevice device, final int mtu);
}
