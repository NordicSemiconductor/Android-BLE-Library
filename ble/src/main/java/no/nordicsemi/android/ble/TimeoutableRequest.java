package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

public abstract class TimeoutableRequest extends Request {
	private Runnable timeoutCallback;
	protected long timeout;

	TimeoutableRequest(@NonNull final Type type) {
		super(type);
	}

	TimeoutableRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	TimeoutableRequest(@NonNull final Type type, @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	@NonNull
	@Override
	TimeoutableRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public TimeoutableRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	/**
	 * Sets the operation timeout.
	 * When the timeout occurs, the request will fail with {@link FailCallback#REASON_TIMEOUT}.
	 *
	 * @param timeout the request timeout in milliseconds, 0 to disable timeout.
	 * @return the callback.
	 * @throws IllegalStateException         thrown when the request has already been started.
	 * @throws UnsupportedOperationException thrown when the timeout is not allowed for this request,
	 *                                       as the callback from the system is required.
	 */
	@NonNull
	public TimeoutableRequest timeout(@IntRange(from = 0) final long timeout) {
		if (timeoutCallback != null)
			throw new IllegalStateException("Request already started");
		this.timeout = timeout;
		return this;
	}

	/**
	 * Enqueues the request for asynchronous execution.
	 * <p>
	 * Use {@link #timeout(long)} to set the maximum time the manager should wait until the device
	 * is ready. When the timeout occurs, the request will fail with
	 * {@link FailCallback#REASON_TIMEOUT} and the device will get disconnected.
	 */
	@Override
	public final void enqueue() {
		super.enqueue();
	}

	/**
	 * Enqueues the request for asynchronous execution.
	 * <p>
	 * When the timeout occurs, the request will fail with {@link FailCallback#REASON_TIMEOUT}
	 * and the device will get disconnected.
	 *
	 * @param timeout the request timeout in milliseconds, 0 to disable timeout. This value will
	 *                override one set in {@link #timeout(long)}.
	 * @deprecated Use {@link #timeout(long)} and {@link #enqueue()} instead.
	 */
	@Deprecated
	public final void enqueue(@IntRange(from = 0) final long timeout) {
		timeout(timeout).enqueue();
	}

	/**
	 * Synchronously waits until the request is done.
	 * <p>
	 * Use {@link #timeout(long)} to set the maximum time the manager should wait until the request
	 * is ready. When the timeout occurs, the {@link InterruptedException} will be thrown.
	 * <p>
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)}
	 * will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @throws RequestFailedException      thrown when the BLE request finished with status other
	 *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws InterruptedException        thrown if the timeout occurred before the request has
	 *                                     finished.
	 * @throws IllegalStateException       thrown when you try to call this method from the main
	 *                                     (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
	 * @throws InvalidRequestException     thrown when the request was called before the device was
	 *                                     connected at least once (unknown device).
	 * @see #enqueue()
	 */
	public final void await() throws RequestFailedException, DeviceDisconnectedException,
			BluetoothDisabledException, InvalidRequestException, InterruptedException {
		assertNotMainThread();

		final SuccessCallback sc = successCallback;
		final FailCallback fc = failCallback;
		try {
			syncLock.close();
			final RequestCallback callback = new RequestCallback();
			done(callback).fail(callback).invalid(callback).enqueue();

			if (!syncLock.block(timeout)) {
				throw new InterruptedException();
			}
			if (!callback.isSuccess()) {
				if (callback.status == FailCallback.REASON_DEVICE_DISCONNECTED) {
					throw new DeviceDisconnectedException();
				}
				if (callback.status == FailCallback.REASON_BLUETOOTH_DISABLED) {
					throw new BluetoothDisabledException();
				}
				if (callback.status == RequestCallback.REASON_REQUEST_INVALID) {
					throw new InvalidRequestException(this);
				}
				throw new RequestFailedException(this, callback.status);
			}
		} finally {
			successCallback = sc;
			failCallback = fc;
		}
	}

	/**
	 * Synchronously waits, for as most as the given number of milliseconds, until the request
	 * is ready.
	 * <p>
	 * When the timeout occurs, the {@link InterruptedException} will be thrown.
	 * <p>
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)}
	 * will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param timeout optional timeout in milliseconds, 0 to disable timeout. This will
	 *                override the timeout set using {@link #timeout(long)}.
	 * @throws RequestFailedException      thrown when the BLE request finished with status other
	 *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws InterruptedException        thrown if the timeout occurred before the request has
	 *                                     finished.
	 * @throws IllegalStateException       thrown when you try to call this method from the main
	 *                                     (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
	 * @throws InvalidRequestException     thrown when the request was called before the device was
	 *                                     connected at least once (unknown device).
	 * @deprecated Use {@link #timeout(long)} and {@link #await()} instead.
	 */
	@Deprecated
	public final void await(@IntRange(from = 0) final long timeout) throws RequestFailedException,
			InterruptedException, DeviceDisconnectedException, BluetoothDisabledException,
			InvalidRequestException {
		timeout(timeout).await();
	}

	@Override
	void notifyStarted(@NonNull final BluetoothDevice device) {
		if (timeout > 0L) {
			timeoutCallback = () -> {
				timeoutCallback = null;
				if (!finished) {
					notifyFail(device, FailCallback.REASON_TIMEOUT);
					requestHandler.onRequestTimeout(this);
				}
			};
			handler.postDelayed(timeoutCallback, timeout);
		}
		super.notifyStarted(device);
	}

	@Override
	void notifySuccess(@NonNull final BluetoothDevice device) {
		if (!finished) {
			handler.removeCallbacks(timeoutCallback);
			timeoutCallback = null;
		}
		super.notifySuccess(device);
	}

	@Override
	void notifyFail(@NonNull final BluetoothDevice device, final int status) {
		if (!finished) {
			handler.removeCallbacks(timeoutCallback);
			timeoutCallback = null;
		}
		super.notifyFail(device, status);
	}

	@Override
	void notifyInvalidRequest() {
		if (!finished) {
			handler.removeCallbacks(timeoutCallback);
			timeoutCallback = null;
		}
		super.notifyInvalidRequest();
	}
}
