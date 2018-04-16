package no.nordicsemi.android.ble.callback;

import android.support.annotation.NonNull;

public interface DataCallback {

	/**
	 * Callback received each time the value was read or has changed using notifications or indications.
	 * @param data the data received. If the {@link DataMerger} was used, this contains the merged result.
	 */
	void onDataReceived(final @NonNull Data data);
}
