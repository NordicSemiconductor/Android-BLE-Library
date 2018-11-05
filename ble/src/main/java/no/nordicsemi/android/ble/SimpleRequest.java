package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGatt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * A request that requires a {@link android.bluetooth.BluetoothGattCallback callback} or can't
 * have timeout for any other reason. This class defines the {@link #await()} method.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SimpleRequest extends Request {

	SimpleRequest(@NonNull final Type type) {
		super(type);
	}

	/**
	 * This method throws an {@link UnsupportedOperationException} exception, as this request does
	 * not support timeout.
	 *
	 * @param timeout the request timeout in milliseconds, 0 to disable timeout. Ignored.
	 * @return This method always throws an exception.
	 * @throws UnsupportedOperationException always.
	 */
	@NonNull
	@Override
	final ReadRequest timeout(@IntRange(from = 0) final long timeout) {
		throw new UnsupportedOperationException("This request may not have timeout");
	}

	/**
	 * Synchronously waits until the request is done.
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
	public final void await() throws RequestFailedException, DeviceDisconnectedException,
			BluetoothDisabledException, InvalidRequestException {
		awaitWithoutTimeout();
	}
}
