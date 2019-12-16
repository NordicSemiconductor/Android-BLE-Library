package no.nordicsemi.android.ble;


import android.os.Handler;

import androidx.annotation.NonNull;

interface RequestHandler {
	/**
	 * Enqueues the given request at the end of the the init or task queue, depending
	 * on whether the initialization is in progress, or not.
	 *
	 * @param request the request to be added.
	 */
	void enqueue(@NonNull final Request request);

	/**
	 * Enqueues the given request at the front of the the init or task queue, depending
	 * on whether the initialization is in progress, or not.
	 *
	 * @param request the request to be added.
	 */
	void enqueueFirst(@NonNull final Request request);

	/**
	 * Removes all enqueued requests from the queue.
	 */
	void cancelQueue();

	/**
	 * Method called when the request timed out.
	 *
	 * @param request the request that timed out.
	 */
	void onRequestTimeout(@NonNull final TimeoutableRequest request);

	/**
	 * Returns the handler to invoke callbacks on.
	 *
	 * @return the handler.
	 */
	Handler getHandler();
}
