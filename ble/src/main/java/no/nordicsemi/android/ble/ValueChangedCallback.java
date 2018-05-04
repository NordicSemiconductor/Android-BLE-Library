package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.os.ConditionVariable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.ReadProgressCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataStream;

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
	 *
	 * @return the object set with {@link #with(DataReceivedCallback)}, or null if this methods wasn't called.
	 * @throws IllegalStateException thrown when you try to call this method from the main (UI)
	 *                               thread.
	 */
	@Nullable
	public DataReceivedCallback await() {
		try {
			return await(0);
		} catch (final InterruptedException e) {
			// never happen
			return null;
		}
	}

	/**
	 * Synchronously waits for a notification or an indication on the requested characteristic,
	 * for at most given number of milliseconds.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @param timeout optional timeout in milliseconds
	 * @throws InterruptedException  thrown if the timeout occurred before the request has finished.
	 * @throws IllegalStateException thrown when you try to call this method from the main (UI)
	 *                               thread.
	 */
	@Nullable
	public DataReceivedCallback await(final int timeout) throws InterruptedException {
		Request.assertNotMainThread();

		syncLock.close();
		if (!syncLock.block(timeout)) {
			throw new InterruptedException();
		}
		return valueCallback;
	}

	void notifyValueChanged(final BluetoothDevice device, final byte[] value) {
		// With no value callback there is no need for any merging
		if (valueCallback == null)
			return;

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
