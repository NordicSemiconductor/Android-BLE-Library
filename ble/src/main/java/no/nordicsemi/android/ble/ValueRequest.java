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
 * A type or Request that holds a value. A value can be received or sent.
 * This is a base class for other types of requests.
 *
 * @param <T> The sent/received value callback type.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ValueRequest<T> extends ConnectionRequest {
	T valueCallback;

	ValueRequest(@NonNull final Type type) {
		super(type);
	}

	ValueRequest(@NonNull final Type type,
                 @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ValueRequest(@NonNull final Type type,
                 @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	/**
	 * Sets the value callback. When the request is invoked synchronously, this callback will
	 * be ignored and the received value will be returned by the <code>await(...)</code> method;
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	public ValueRequest<T> with(@NonNull final T callback) {
		this.valueCallback = callback;
		return this;
	}

	@SuppressWarnings("ConstantConditions")
	@NonNull
	<E extends T> E awaitWithoutTimeout(@NonNull final Class<E> responseClass)
			throws RequestFailedException, DeviceDisconnectedException, BluetoothDisabledException,
			InvalidRequestException {
		try {
			return awaitWithTimeout(responseClass);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	@NonNull
	<E extends T> E awaitWithoutTimeout(@NonNull final E response)
			throws RequestFailedException, DeviceDisconnectedException, BluetoothDisabledException,
			InvalidRequestException {
		try {
			return awaitWithTimeout(response);
		} catch (final InterruptedException e) {
			// never happen
			throw new IllegalStateException("This should never happen");
		}
	}

	@SuppressWarnings("ConstantConditions")
	@NonNull
	<E extends T> E awaitWithTimeout(@NonNull final Class<E> responseClass)
			throws RequestFailedException, DeviceDisconnectedException, BluetoothDisabledException,
			InvalidRequestException, InterruptedException {
		assertNotMainThread();

		try {
			final E response = responseClass.newInstance();
			return awaitWithTimeout(response);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Couldn't instantiate "
					+ responseClass.getCanonicalName()
					+ " class. Is the default constructor accessible?");
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Couldn't instantiate "
					+ responseClass.getCanonicalName()
					+ " class. Does it have a default constructor with no arguments?");
		}
	}

	@NonNull
	<E extends T> E awaitWithTimeout(@NonNull final E response)
			throws RequestFailedException, DeviceDisconnectedException, BluetoothDisabledException,
			InvalidRequestException, InterruptedException {
		assertNotMainThread();

		final SuccessCallback sc = successCallback;
		final FailCallback fc = failCallback;
		final T vc = valueCallback;
		try {
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
				if (callback.status == RequestCallback.REASON_REQUEST_INVALID) {
					throw new InvalidRequestException(this);
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
}
