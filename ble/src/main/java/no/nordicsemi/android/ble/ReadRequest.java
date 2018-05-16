package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.ReadProgressCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataStream;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

@SuppressWarnings("unused")
public final class ReadRequest extends Request<DataReceivedCallback> {
	private ReadProgressCallback progressCallback;
	private DataMerger dataMerger;
	private DataStream buffer;
	private int count = 0;

	ReadRequest(final @NonNull Type type) {
		super(type);
	}

	ReadRequest(final @NonNull Type type, final @Nullable BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ReadRequest(final @NonNull Type type, final @Nullable BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	@Override
	@NonNull
	public ReadRequest done(final @NonNull SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public ReadRequest fail(final @NonNull FailCallback callback) {
		this.failCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public ReadRequest with(final @NonNull DataReceivedCallback callback) {
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
	public ReadRequest merge(final @NonNull DataMerger merger) {
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
	public ReadRequest merge(final @NonNull DataMerger merger, final @NonNull ReadProgressCallback callback) {
		this.dataMerger = merger;
		this.progressCallback = callback;
		return this;
	}

	/**
	 * Same as {@link #await(Class)}, but if the response class extends {@link ProfileReadResponse}
	 * and the received response is not valid, this method will thrown an exception instead of
	 * just returning a response with {@link ProfileReadResponse#isValid()} returning false.
	 *
	 * @param responseClass the result class. This class will be instantiate, therefore it has to have
	 *                      a default constructor.
	 * @return the object with the response
	 * @throws RequestFailedException thrown when the BLE request finished with status other than
	 *                                {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException  thrown when you try to call this method from the main (UI)
	 *                                thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 */
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(final @NonNull Class<E> responseClass)
			throws RequestFailedException, InvalidDataException, DeviceDisconnectedException {
		E response = await(responseClass);
		if (!response.isValid()) {
			throw new InvalidDataException(response);
		}
		return response;
	}

	/**
	 * Same as {@link #await(Class, int)}, but if the response class extends {@link ProfileReadResponse}
	 * and the received response is not valid, this method will thrown an exception instead of
	 * just returning a response with {@link ProfileReadResponse#isValid()} returning false.
	 *
	 * @param responseClass the result class. This class will be instantiate, therefore it has to have
	 *                    a default constructor.
	 * @param timeout     optional timeout in milliseconds
	 * @return the object with the response
	 * @throws RequestFailedException thrown when the BLE request finished with status other than
	 *                                {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws InterruptedException   thrown if the timeout occurred before the request has finished.
	 * @throws IllegalStateException  thrown when you try to call this method from the main (UI)
	 *                                thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 */
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(final @NonNull Class<E> responseClass, final int timeout)
			throws RequestFailedException, InterruptedException, InvalidDataException, DeviceDisconnectedException {
		E response = await(responseClass, timeout);
		if (!response.isValid()) {
			throw new InvalidDataException(response);
		}
		return response;
	}

	void notifyValueChanged(final @NonNull BluetoothDevice device, final byte[] value) {
		// Keep a reference to the value callback, as it may change during execution
		final DataReceivedCallback valueCallback = this.valueCallback;

		// With no value callback there is no need for any merging
		if (valueCallback == null)
			return;

		if (dataMerger == null) {
			valueCallback.onDataReceived(device, new Data(value));
		} else {
			if (progressCallback != null)
				progressCallback.onPacketReceived(device, value, count);
			if (buffer == null)
				buffer = new DataStream();
			if (dataMerger.merge(buffer, value, count++)) {
				valueCallback.onDataReceived(device, buffer.toData());
				buffer = null;
				count = 0;
			} // else
			// wait for more packets to be merged
		}
	}

	boolean hasMore() {
		return count > 0;
	}
}
