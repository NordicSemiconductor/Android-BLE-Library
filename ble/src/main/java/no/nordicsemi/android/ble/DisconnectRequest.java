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
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

@SuppressWarnings({"WeakerAccess", "unused"})
public class DisconnectRequest extends Request {

	DisconnectRequest(@NonNull final Type type) {
		super(type);
	}

	@NonNull
	@Override
	DisconnectRequest setManager(@NonNull final BleManager manager) {
		super.setManager(manager);
		return this;
	}

	@NonNull
	@Override
	public DisconnectRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@NonNull
	@Override
	public DisconnectRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public DisconnectRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public DisconnectRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	@NonNull
	@Override
	public DisconnectRequest timeout(@IntRange(from = 0) final long timeout) {
		super.timeout(timeout);
		return this;
	}

	/**
	 * Enqueues the disconnect request for asynchronous execution.
	 * <p>
	 * Use {@link #timeout(long)} to set the maximum time the manager will wait.
	 * When the timeout occurs, the request will fail with {@link FailCallback#REASON_TIMEOUT}
	 * and the connection will be force-closed using {@link BleManager#clone()}.
	 */
	@Override
	public void enqueue() {
		super.enqueue();
	}

	/**
	 * Enqueues the disconnect request for asynchronous execution.
	 * <p>
	 * Use {@link #timeout(long)} to set the maximum time the manager will wait.
	 * When the timeout occurs, the request will fail with {@link FailCallback#REASON_TIMEOUT}
	 * and the connection will be force-closed using {@link BleManager#clone()}.
	 *
	 * @param timeout the request timeout in milliseconds, 0 to disable timeout. This value
	 *                will override one set using {@link #timeout(long)}.
	 * @deprecated Use {@link #timeout(long)} and {@link #enqueue()} instead.
	 */
	@Deprecated
	public void enqueue(@IntRange(from = 0) final long timeout) {
		timeout(timeout).enqueue();
	}

	/**
	 * Synchronously waits until the device disconnects.
	 * <p>
	 * Use {@link #timeout(long)} to set the maximum time the manager will wait.
	 * When the timeout occurs, the request will throw {@link InterruptedException}
	 * and the connection will be force-closed using {@link BleManager#clone()}.
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
	 */
	public void await() throws RequestFailedException, DeviceDisconnectedException,
			BluetoothDisabledException, InvalidRequestException, InterruptedException {
		awaitWithTimeout();
	}

	/**
	 * Synchronously waits until the device disconnects for at most the given number of
	 * milliseconds. When the timeout occurs, the request will throw {@link InterruptedException}
	 * and the connection will be force-closed using {@link BleManager#clone()}.
	 * <p>
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)}
	 * will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 *
	 * @param timeout optional timeout in milliseconds, 0 to disable timeout.
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
	public void await(@IntRange(from = 0) final long timeout) throws RequestFailedException,
			InterruptedException, DeviceDisconnectedException, BluetoothDisabledException,
			InvalidRequestException {
		timeout(timeout).awaitWithTimeout();
	}
}
