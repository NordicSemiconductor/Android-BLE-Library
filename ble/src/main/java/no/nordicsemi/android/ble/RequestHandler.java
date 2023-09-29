package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

abstract class RequestHandler implements CallbackHandler {
	/**
	 * Enqueues the given request at the end of the the init or task queue, depending
	 * on whether the initialization is in progress, or not.
	 * <p>
	 * This method will automatically try to execute the next request (not necessarily the
	 * enqueued one).
	 *
	 * @param request the request to be added.
	 */
	abstract void enqueue(@NonNull final Request request);

	/**
	 * Removes all enqueued requests from the queue.
	 */
	abstract void cancelQueue();

	/**
	 * Cancels current {@link TimeoutableRequest}.
	 */
	abstract void cancelCurrent();

	/**
	 * Method called when the request timed out.
	 *
	 * @param request the request that timed out.
	 */
	abstract void onRequestTimeout(
			@NonNull final BluetoothDevice device,
			@NonNull final TimeoutableRequest request
	);
}
