package no.nordicsemi.android.ble;

import androidx.annotation.NonNull;

interface CallbackHandler {

	/**
	 * Causes the Runnable r to be added to the message queue.
	 * The runnable will be run on the thread to which this handler is
	 * attached.
	 *
	 * @param r The Runnable that will be executed.
	 */
	void post(@NonNull final Runnable r);

	/**
	 * Causes the Runnable r to be added to the message queue, to be run
	 * after the specified amount of time elapses.
	 * The runnable will be run on the thread to which this handler
	 * is attached.
	 * <b>The time-base is {@link android.os.SystemClock#uptimeMillis}.</b>
	 * Time spent in deep sleep will add an additional delay to execution.
	 *
	 * @param r The Runnable that will be executed.
	 * @param delayMillis The delay (in milliseconds) until the Runnable
	 *        will be executed.
	 */
	void postDelayed(@NonNull final Runnable r, final long delayMillis);

	/**
	 * Remove any pending posts of Runnable r that are in the message queue.
     */
	void removeCallbacks(@NonNull final Runnable r);
}
