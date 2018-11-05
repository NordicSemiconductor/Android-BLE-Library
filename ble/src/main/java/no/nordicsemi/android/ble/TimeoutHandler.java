package no.nordicsemi.android.ble;

import android.support.annotation.NonNull;

abstract class TimeoutHandler {

	/**
	 * Method called when the request timed out.
	 *
	 * @param request the request that timed out.
	 */
	abstract void onRequestTimeout(@NonNull final TimeoutableRequest request);
}
