package no.nordicsemi.android.ble.callback;

import android.support.annotation.NonNull;

public interface ValueCallback {

	/**
	 * Callback received each time the value was read or has changed using notifications or indications.
	 * @param value the full messaged. If the {@link ValueMerger} was used, this contains the merged result.
	 */
	void onValueChanged(final @NonNull byte[] value);
}
