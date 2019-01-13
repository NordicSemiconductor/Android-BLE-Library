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

package no.nordicsemi.android.ble.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface FailCallback {
	int REASON_DEVICE_DISCONNECTED = -1;
	int REASON_DEVICE_NOT_SUPPORTED = -2;
	int REASON_NULL_ATTRIBUTE = -3;
	int REASON_REQUEST_FAILED = -4;
	int REASON_TIMEOUT = -5;
	int REASON_VALIDATION = -6;
	int REASON_BLUETOOTH_DISABLED = -100;

	/**
	 * A callback invoked when the request has failed with status other than
	 * {@link android.bluetooth.BluetoothGatt#GATT_SUCCESS}.
	 *
	 * @param device target device.
	 * @param status error status code, one of BluetoothGatt#GATT_* constants or
	 *               {@link #REASON_DEVICE_DISCONNECTED}, {@link #REASON_TIMEOUT},
	 *               {@link #REASON_DEVICE_NOT_SUPPORTED} (only for Connect request),
	 *               {@link #REASON_BLUETOOTH_DISABLED}, {@link #REASON_NULL_ATTRIBUTE},
	 *               {@link #REASON_VALIDATION} or {@link #REASON_REQUEST_FAILED} (for other reason).
	 */
	void onRequestFailed(@NonNull final BluetoothDevice device, final int status);
}
