package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

/**
 * The connection parameters for a Bluetooth LE connection is a set of parameters that determine
 * when and how the Central and a Peripheral in a link transmits data.
 * It is always the Central that actually sets the connection parameters used, but the Peripheral
 * can send a so-called Connection Parameter Update Request, that the Central can then accept or reject.
 * <p>
 * On Android, requesting connection parameters is available since Android Lollipop using
 * {@link android.bluetooth.BluetoothGatt#requestConnectionPriority(int)}. There are 3 options
 * available: {@link android.bluetooth.BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER},
 * {@link android.bluetooth.BluetoothGatt#CONNECTION_PRIORITY_BALANCED} and
 * {@link android.bluetooth.BluetoothGatt#CONNECTION_PRIORITY_HIGH}. See
 * {@link no.nordicsemi.android.ble.Request#newConnectionPriorityRequest(int)} for details.
 * <p>
 * Until Android 8.0 Oreo, there was no callback indicating whether the change has succeeded,
 * or not. Also, when a Central or Peripheral requested connection parameters change without
 * explicit calling of this method, the application was not aware of it.
 * Android Oreo added a hidden callback to {@link android.bluetooth.BluetoothGattCallback}
 * notifying about connection parameters change. Those values will be reported with this callback.
 */
public interface ConnectionPriorityCallback {

	/**
	 * Callback indicating the connection parameters were updated. Works on Android 8.0 Oreo or newer.
	 *
	 * @param device   target device.
	 * @param interval Connection interval used on this connection, 1.25ms unit. Valid range is from
	 *                 6 (7.5ms) to 3200 (4000ms).
	 * @param latency  Slave latency for the connection in number of connection events. Valid range
	 *                 is from 0 to 499
	 * @param timeout  Supervision timeout for this connection, in 10ms unit. Valid range is from 10
	 *                 (0.1s) to 3200 (32s)
	 */
	void onConnectionUpdated(final @NonNull BluetoothDevice device, final int interval, final int latency, final int timeout);
}
