package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * A value request that requires a {@link android.bluetooth.BluetoothGattCallback callback} or
 * can't have timeout for any other reason. This class defines the {@link #await()} methods.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class SimpleValueRequest<T> extends ValueRequest<T> {

	SimpleValueRequest(@NonNull final Type type) {
		super(type);
	}

	SimpleValueRequest(@NonNull final Type type,
					   @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	SimpleValueRequest(@NonNull final Type type,
					   @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	/**
	 * Synchronously waits until the request is done.
	 * <p>
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)}
	 * will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @throws RequestFailedException      thrown when the BLE request finished with status other
	 *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException       thrown when you try to call this method from the main
	 *                                     (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
	 * @throws InvalidRequestException     thrown when the request was called before the device was
	 *                                     connected at least once (unknown device).
	 */
	public void await() throws RequestFailedException, DeviceDisconnectedException,
			BluetoothDisabledException, InvalidRequestException {
		awaitWithoutTimeout();
	}

	/**
	 * Synchronously waits until the request is done.
	 * <p>
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param responseClass the response class. This class will be instantiate, therefore it has
	 *                      to have a default constructor.
	 * @return The response with a response.
	 * @throws RequestFailedException      thrown when the BLE request finished with status other
	 *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException       thrown when you try to call this method from the main
	 *                                     (UI) thread.
	 * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter is disabled.
	 * @throws InvalidRequestException     thrown when the request was called before the device was
	 *                                     connected at least once (unknown device).
	 * @see #await(Object)
	 */
	@NonNull
	<E extends T> E await(@NonNull final Class<E> responseClass) throws RequestFailedException,
			DeviceDisconnectedException, BluetoothDisabledException, InvalidRequestException {
		return awaitWithoutTimeout(responseClass);
	}

	/**
	 * Synchronously waits until the request is done. The given response object will be filled
	 * with the request response.
	 * <p>
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param response the response object.
	 * @param <E>      a response class.
	 * @return The response with a response.
	 * @throws RequestFailedException      thrown when the BLE request finished with status other
	 *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException       thrown when you try to call this method from the main
	 *                                     (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter is disabled.
	 * @throws InvalidRequestException     thrown when the request was called before the device was
	 *                                     connected at least once (unknown device).
	 * @see #await(Class)
	 */
	@NonNull
	<E extends T> E await(@NonNull final E response) throws RequestFailedException,
			DeviceDisconnectedException, BluetoothDisabledException, InvalidRequestException {
		return awaitWithoutTimeout(response);
	}
}
