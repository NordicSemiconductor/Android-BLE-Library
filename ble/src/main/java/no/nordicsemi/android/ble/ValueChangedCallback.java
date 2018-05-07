package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.os.ConditionVariable;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.ReadProgressCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataStream;
import no.nordicsemi.android.ble.exception.InvalidDataException;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class ValueChangedCallback {
	private ReadProgressCallback progressCallback;
	private DataReceivedCallback valueCallback;
	private DataMerger dataMerger;
	private DataStream buffer;
	private final ConditionVariable syncLock;
	private int count = 0;

	ValueChangedCallback() {
		syncLock = new ConditionVariable(true);
	}

	@NonNull
	public ValueChangedCallback with(@NonNull final DataReceivedCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return the request
	 */
	@NonNull
	public ValueChangedCallback merge(final @NonNull DataMerger merger) {
		this.dataMerger = merger;
		this.progressCallback = null;
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return the request
	 */
	@NonNull
	public ValueChangedCallback merge(final @NonNull DataMerger merger, final @NonNull ReadProgressCallback callback) {
		this.dataMerger = merger;
		this.progressCallback = callback;
		return this;
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 * <p>
	 * The value of returned notification or indication is ignored.
	 * </p>
	 *
	 * @throws IllegalStateException thrown when you try to call this method from the main (UI)
	 *                               thread.
	 */
	@SuppressWarnings("ConstantConditions")
	public void await() {
		await(null);
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic,
	 * for at most given number of milliseconds.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 * <p>
	 * The value of returned notification or indication is ignored.
	 * </p>
	 *
	 * @param timeout       optional timeout in milliseconds
	 * @throws IllegalStateException thrown when you try to call this method from the main (UI)
	 *                               thread.
	 */
	@SuppressWarnings("ConstantConditions")
	public void await(final int timeout) throws InterruptedException {
		await(null, timeout);
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @param responseClass the response class. This class will be instantiate, therefore it has to have
	 *                      a default constructor.
	 * @return the object received with a notification or indication
	 * @throws IllegalStateException thrown when you try to call this method from the main (UI)
	 *                               thread.
	 */
	@SuppressWarnings("NullableProblems")
	@NonNull
	public <E extends DataReceivedCallback> E await(final @NonNull Class<E> responseClass) {
		try {
			return await(responseClass, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic,
	 * for at most given number of milliseconds.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @param responseClass the response class. This class will be instantiate, therefore it has to have
	 *                      a default constructor.
	 * @param timeout       optional timeout in milliseconds
	 * @return the object received with a notification or indication
	 * @throws InterruptedException  thrown if the timeout occurred before the request has finished.
	 * @throws IllegalStateException thrown when you try to call this method from the main (UI)
	 *                               thread.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends DataReceivedCallback> E await(final @NonNull Class<E> responseClass, final int timeout)
			throws InterruptedException {
		Request.assertNotMainThread();

		final DataReceivedCallback vc = valueCallback;
		try {
			E response = null;
			if (responseClass != null)
				response = responseClass.newInstance();
			syncLock.close();
			with(response);

			if (!syncLock.block(timeout)) {
				throw new InterruptedException();
			}
			return response;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + responseClass.getCanonicalName()
					+ " class. Is the default constructor accessible?");
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + responseClass.getCanonicalName()
					+ " class. Does it have a default constructor with no arguments?");
		} finally {
			valueCallback = vc;
		}
	}

	/**
	 * Same as {@link #await(Class, int)}, but if the response class extends {@link ProfileReadResponse}
	 * and the received response is not valid, this method will thrown an exception instead of
	 * just returning a response with {@link ProfileReadResponse#isValid()} returning false.
	 *
	 * @param responseClass the result class. This class will be instantiate, therefore it has to have
	 *                      a default constructor.
	 * @return the object with the response
	 * @throws IllegalStateException thrown when you try to call this method from the main (UI)
	 *                               thread.
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	<E extends ProfileReadResponse> E awaitForValid(final @NonNull Class<E> responseClass, final int timeout)
			throws InterruptedException, InvalidDataException {
		final E response = await(responseClass, timeout);
		if (response != null && !response.isValid()) {
			throw new InvalidDataException(response);
		}
		return response;
	}

	void notifyValueChanged(final BluetoothDevice device, final byte[] value) {
		// With no value callback there is no need for any merging
		if (valueCallback == null) {
			syncLock.open();
			return;
		}

		if (dataMerger == null) {
			valueCallback.onDataReceived(device, new Data(value));
			syncLock.open();
		} else {
			if (progressCallback != null)
				progressCallback.onPacketReceived(device, value, count);
			if (buffer == null)
				buffer = new DataStream();
			if (dataMerger.merge(buffer, value, count++)) {
				valueCallback.onDataReceived(device, buffer.toData());
				buffer = null;
				count = 0;
				syncLock.open();
			} // else
			// wait for more packets to be merged
		}
	}
}
