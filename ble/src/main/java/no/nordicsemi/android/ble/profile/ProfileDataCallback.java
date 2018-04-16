package no.nordicsemi.android.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.callback.DataCallback;
import no.nordicsemi.android.ble.callback.DataMerger;

public interface ProfileDataCallback extends DataCallback {

	/**
	 * Callback called when the data received do not conform to required scheme.
	 * @param device target device.
	 * @param data the data received. If the {@link DataMerger} was used, this contains the merged result.
	 */
	default void onInvalidDataReceived(final @NonNull BluetoothDevice device, final @NonNull Data data) {
		// ignore
	}
}
