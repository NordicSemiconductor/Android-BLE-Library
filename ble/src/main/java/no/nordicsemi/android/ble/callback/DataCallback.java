package no.nordicsemi.android.ble.callback;

import android.support.annotation.NonNull;

public interface DataCallback {

	/**
	 * Callback received each time the value was read or has changed using notifications or indications.
	 * @param data the full messaged. If the {@link ValueMerger} was used, this contains the merged result.
	 */
	void onValueChanged(final @NonNull Data data);
}
