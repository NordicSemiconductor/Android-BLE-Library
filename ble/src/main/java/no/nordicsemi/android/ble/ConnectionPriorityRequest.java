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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import no.nordicsemi.android.ble.callback.ConnectionPriorityCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

public final class ConnectionPriorityRequest extends ValueRequest<ConnectionPriorityCallback> {
	private ConnectionPriorityCallback valueCallback;
	private final int value;

	ConnectionPriorityRequest(@NonNull final Type type, int priority) {
		super(type);
		if (priority < 0 || priority > 2)
			priority = 0; // Balanced
		this.value = priority;
	}

	@Override
	@NonNull
	public ConnectionPriorityRequest done(@NonNull final SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public ConnectionPriorityRequest fail(@NonNull final FailCallback callback) {
		this.failCallback = callback;
		return this;
	}

	@RequiresApi(value = Build.VERSION_CODES.O)
	@Override
	@NonNull
	public ConnectionPriorityRequest with(@NonNull final ConnectionPriorityCallback callback) {
		// The BluetoothGattCallback#onConnectionUpdated callback was introduced in Android Oreo.
		this.valueCallback = callback;
		return this;
	}

	@RequiresApi(value = Build.VERSION_CODES.O)
	@NonNull
	@Override
	public <E extends ConnectionPriorityCallback> E await(final Class<E> responseClass)
			throws RequestFailedException, DeviceDisconnectedException, BluetoothDisabledException {
		// The BluetoothGattCallback#onConnectionUpdated callback was introduced in Android Oreo.
		return super.await(responseClass);
	}

	@RequiresApi(value = Build.VERSION_CODES.O)
	@NonNull
	@Override
	public <E extends ConnectionPriorityCallback> E await(@NonNull final Class<E> responseClass,
														  final int timeout)
			throws RequestFailedException, InterruptedException, DeviceDisconnectedException,
			BluetoothDisabledException {
		// The BluetoothGattCallback#onConnectionUpdated callback was introduced in Android Oreo.
		return super.await(responseClass, timeout);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	void notifyConnectionPriorityChanged(@NonNull final BluetoothDevice device,
										 final int interval, final int latency, final int timeout) {
		if (valueCallback != null)
			valueCallback.onConnectionUpdated(device, interval, latency, timeout);
	}

	int getRequiredPriority() {
		return value;
	}
}
