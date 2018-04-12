package no.nordicsemi.android.ble;

public interface RequestCompletionCallback {

	/**
	 * A callback invoked
	 * @param success
	 */
	void onRequestCompleted(final boolean success);
}
