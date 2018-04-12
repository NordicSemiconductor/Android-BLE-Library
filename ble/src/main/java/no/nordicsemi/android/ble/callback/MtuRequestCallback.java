package no.nordicsemi.android.ble.callback;

public interface MtuRequestCallback {

	/**
	 * Method called when the MTU request has finished with success. The MTU value may
	 * be different than requested one.
	 *
	 * @param mtu the new MTU (Maximum Transfer Unit)
	 */
	void onConnectionUpdated(final int mtu);
}
