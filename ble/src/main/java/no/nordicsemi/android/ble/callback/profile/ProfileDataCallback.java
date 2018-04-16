package no.nordicsemi.android.ble.callback.profile;

import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.callback.DataCallback;
import no.nordicsemi.android.ble.callback.DataMerger;

public interface ProfileDataCallback extends DataCallback {

	/**
	 * Callback called when the data received do not conform to required scheme.
	 * @param data the data received. If the {@link DataMerger} was used, this contains the merged result.
	 */
	void onInvalidDataReceived(final @NonNull Data data);
}
