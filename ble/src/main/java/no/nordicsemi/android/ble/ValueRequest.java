package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * A type or Request that holds a value. A value can be received or sent.
 * This is a base class for other types of requests.
 *
 * @param <T> The sent/received value callback type.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ValueRequest<T> extends Request {
	T valueCallback;

	ValueRequest(@NonNull final Type type) {
		super(type);
	}

	ValueRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ValueRequest(@NonNull final Type type, @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	@NonNull
	@Override
	public ValueRequest<T> done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@NonNull
	@Override
	public ValueRequest<T> fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	/**
	 * Sets the value callback. When {@link #await(int)} is used this callback will be returned
	 * by that method.
	 *
	 * @param callback the callback
	 * @return the request
	 */
	@NonNull
	protected ValueRequest<T> with(final @NonNull T callback) {
		this.valueCallback = callback;
		return this;
	}

	/**
	 * Synchronously waits until the request is done.
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @param responseClass the response class. This class will be instantiate, therefore it has to have
	 *                      a default constructor.
	 * @return the response with a response
	 * @throws RequestFailedException      thrown when the BLE request finished with status other than
	 *                                     {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException       thrown when you try to call this method from the main (UI)
	 *                                     thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 */
	@NonNull
	public <E extends T> E await(final Class<E> responseClass)
			throws RequestFailedException, DeviceDisconnectedException {
		try {
			return await(responseClass, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Synchronously waits until the request is done, for at most given number of milliseconds.
	 * Callbacks set using {@link #done(SuccessCallback)}, {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @param responseClass the response class. This class will be instantiate, therefore it has
	 *                      to have a default constructor.
	 * @param timeout       optional timeout in milliseconds
	 * @return the object with a response
	 * @throws RequestFailedException      thrown when the BLE request finished with status other
	 *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws InterruptedException        thrown if the timeout occurred before the request has
	 *                                     finished.
	 * @throws IllegalStateException       thrown when you try to call this method from the main (UI)
	 *                                     thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@NonNull
	public <E extends T> E await(final @NonNull Class<E> responseClass, final int timeout)
			throws RequestFailedException, InterruptedException, DeviceDisconnectedException {
		assertNotMainThread();

		final SuccessCallback sc = successCallback;
		final FailCallback fc = failCallback;
		final T vc = valueCallback;
		try {
			E response = null;
			if (responseClass != null)
				response = responseClass.newInstance();
			syncLock.close();
			final RequestCallback callback = new RequestCallback();
			with(response).done(callback).fail(callback);

			if (!syncLock.block(timeout)) {
				throw new InterruptedException();
			}
			if (!callback.isSuccess()) {
				if (callback.status == FailCallback.REASON_DEVICE_DISCONNECTED) {
					throw new DeviceDisconnectedException();
				}
				throw new RequestFailedException(this, callback.status);
			}
			return response;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + responseClass.getCanonicalName()
					+ " class. Is the default constructor accessible?");
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + responseClass.getCanonicalName()
					+ " class. Does it have a default constructor with no arguments?");
		} finally {
			successCallback = sc;
			failCallback = fc;
			valueCallback = vc;
		}
	}
}
