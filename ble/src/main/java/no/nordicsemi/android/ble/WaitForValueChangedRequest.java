/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
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
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

@SuppressWarnings({"unused", "WeakerAccess"})
public class WaitForValueChangedRequest extends ValueRequest<DataReceivedCallback> {
	private ReadProgressCallback progressCallback;
	private DataMerger dataMerger;
	private DataStream buffer;
	private Request trigger;
	private boolean deviceDisconnected;
	private boolean bluetoothDisabled;
	private int triggerStatus;
	private int count = 0;

	WaitForValueChangedRequest(@NonNull final Type type,
							   @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	@NonNull
	@Override
	WaitForValueChangedRequest setManager(@NonNull final BleManager manager) {
		super.setManager(manager);
		return this;
	}

	@NonNull
	@Override
	public WaitForValueChangedRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@NonNull
	@Override
	public WaitForValueChangedRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public WaitForValueChangedRequest with(@NonNull final DataReceivedCallback callback) {
		super.with(callback);
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return The request.
	 */
	@NonNull
	public WaitForValueChangedRequest merge(@NonNull final DataMerger merger) {
		this.dataMerger = merger;
		this.progressCallback = null;
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return The request.
	 */
	@NonNull
	public WaitForValueChangedRequest merge(@NonNull final DataMerger merger,
											@NonNull final ReadProgressCallback callback) {
		this.dataMerger = merger;
		this.progressCallback = callback;
		return this;
	}

	/**
	 * Sets an optional request that is suppose to trigger the notification or indication.
	 * This is to ensure that the characteristic value won't change before the callback was set.
	 *
	 * @param trigger the operation that triggers the notification, usually a write characteristic
	 *                request that write some OP CODE>
	 * @return The request.
	 */
	@NonNull
	public WaitForValueChangedRequest trigger(@NonNull final Request trigger) {
		this.trigger = trigger;
		this.trigger.internalFail((device, status) -> {
			triggerStatus = status;
			syncLock.open();
			notifyFail(device, status);
		});
		return this;
	}

	@NonNull
	@Override
	public <E extends DataReceivedCallback> E await(@NonNull final E response,
													final int timeout)
			throws RequestFailedException, InterruptedException, DeviceDisconnectedException,
			BluetoothDisabledException {
		assertNotMainThread();

		final SuccessCallback sc = successCallback;
		final FailCallback fc = failCallback;
		final DataReceivedCallback vc = valueCallback;
		try {
			// Ensure the trigger request it enqueued after the callback has been set.
			triggerStatus = BluetoothGatt.GATT_SUCCESS;
			if (trigger != null && trigger.enqueued) {
				throw new IllegalStateException("Trigger request already enqueued");
			}

			syncLock.close();
			final RequestCallback callback = new RequestCallback();
			with(response).done(callback).fail(callback).enqueue();

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
				if (triggerStatus != BluetoothGatt.GATT_SUCCESS) {
					throw new RequestFailedException(trigger, triggerStatus);
				}
				throw new RequestFailedException(this, callback.status);
			}
			return response;
		} finally {
			successCallback = sc;
			failCallback = fc;
			valueCallback = vc;
		}
	}

	/**
	 * Similar to {@link #await(Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is invalid, an exception is thrown.
	 * This allows to keep all error handling in one place.
	 *
	 * @param responseClass the result class. This class will be instantiate, therefore it
	 *                      has to have a default constructor.
	 * @param <E>           a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
	 * @throws InvalidDataException        thrown when the received data were not valid (that is when
	 *                                     {@link ProfileReadResponse#onDataReceived(BluetoothDevice, Data)}
	 *                                     failed to parse the data correctly and called
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}).
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final Class<E> responseClass)
			throws RequestFailedException, InvalidDataException, DeviceDisconnectedException,
			BluetoothDisabledException {
		try {
			return awaitValid(responseClass, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Similar to {@link #await(Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is invalid, an exception is thrown.
	 * This allows to keep all error handling in one place.
	 *
	 * @param response the result object.
	 * @param <E>      a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
	 * @throws InvalidDataException        thrown when the received data were not valid (that is when
	 *                                     {@link ProfileReadResponse#onDataReceived(BluetoothDevice, Data)}
	 *                                     failed to parse the data correctly and called
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}).
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final E response)
			throws RequestFailedException, InvalidDataException, DeviceDisconnectedException,
			BluetoothDisabledException {
		try {
			return awaitValid(response, 0);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	/**
	 * Same as {@link #await(Class, int)}, but if received response is not valid, this method will
	 * thrown an exception.
	 *
	 * @param responseClass the result class. This class will be instantiate, therefore it
	 *                      has to have a default constructor.
	 * @param timeout       optional timeout in milliseconds.
	 * @param <E>           a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
	 * @throws InvalidDataException        thrown when the received data were not valid (that is when
	 *                                     {@link ProfileReadResponse#onDataReceived(BluetoothDevice, Data)}
	 *                                     failed to parse the data correctly and called
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}).
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final Class<E> responseClass,
														final int timeout)
			throws InterruptedException, InvalidDataException, RequestFailedException,
			DeviceDisconnectedException, BluetoothDisabledException {
		final E response = await(responseClass, timeout);
		if (response != null && !response.isValid()) {
			throw new InvalidDataException(response);
		}
		return response;
	}

	/**
	 * Same as {@link #await(Object, int)}, but if received response is not valid, this method will
	 * thrown an exception.
	 *
	 * @param response the result object.
	 * @param timeout  optional timeout in milliseconds.
	 * @param <E>      a response class that extends {@link ProfileReadResponse}.
	 * @return Object with a valid response.
	 * @throws IllegalStateException       thrown when you try to call this method from
	 *                                     the main (UI) thread.
	 * @throws InterruptedException        thrown when the timeout occurred before the
	 *                                     characteristic value has changed.
	 * @throws RequestFailedException      thrown when the trigger request has failed.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the
	 *                                     notification or indication was received.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
	 * @throws InvalidDataException        thrown when the received data were not valid (that is when
	 *                                     {@link ProfileReadResponse#onDataReceived(BluetoothDevice, Data)}
	 *                                     failed to parse the data correctly and called
	 *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}).
	 */
	@SuppressWarnings("ConstantConditions")
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final E response,
														final int timeout)
			throws InterruptedException, InvalidDataException, DeviceDisconnectedException,
			RequestFailedException, BluetoothDisabledException {
		final E result = await(response, timeout);
		if (result != null && !result.isValid()) {
			throw new InvalidDataException(result);
		}
		return result;
	}

	void notifyValueChanged(final BluetoothDevice device, final byte[] value) {
		// Keep a reference to the value callback, as it may change during execution
		final DataReceivedCallback valueCallback = this.valueCallback;

		// With no value callback there is no need for any merging
		if (valueCallback == null) {
			return;
		}

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

	Request getTrigger() {
		return trigger;
	}
}
