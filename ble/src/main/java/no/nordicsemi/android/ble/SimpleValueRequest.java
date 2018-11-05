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

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.IntRange;
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
	 * This method throws an {@link UnsupportedOperationException} exception, as this request does
	 * not support timeout.
	 *
	 * @param timeout the request timeout in milliseconds, 0 to disable timeout. Ignored.
	 * @return This method always throws an exception.
	 * @throws UnsupportedOperationException always.
	 */
	@NonNull
	@Override
	final ConnectionPriorityRequest timeout(@IntRange(from = 0) final long timeout) {
		throw new UnsupportedOperationException("This request may not have timeout");
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
