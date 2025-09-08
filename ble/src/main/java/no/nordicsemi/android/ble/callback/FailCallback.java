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
import android.bluetooth.BluetoothGatt;
import no.nordicsemi.android.ble.BleManager;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface FailCallback {
	int REASON_DEVICE_DISCONNECTED = -1;
	/**
	 * Returned when the {@link BleManager#isRequiredServiceSupported(BluetoothGatt)}
	 * returns false, that is when at least required GATT service was not discovered
	 * on the connected device.
	 */
	int REASON_DEVICE_NOT_SUPPORTED = -2;
	int REASON_NULL_ATTRIBUTE = -3;
	int REASON_REQUEST_FAILED = -4;
	int REASON_TIMEOUT = -5;
	int REASON_VALIDATION = -6;
	int REASON_CANCELLED = -7;
	int REASON_NOT_ENABLED = -8;
	/**
	 * The Android device is unable to reconnect to the peripheral because of internal failure.
	 * Most probably it cannot respond properly to PHY LE 2M update procedure, causing the
	 * remote device to terminate the connection.
	 * <p>
	 * Try disabling PHY LE 2M on the peripheral side, or update the Android version.
	 * If that's not possible, the connection to your device may not work on the given
	 * Android device at all. If the device is bonded, try removing bonding and connect,
	 * but this seems to fix the problem only before a new bond is created.
	 */
	int REASON_UNSUPPORTED_CONFIGURATION = -9;
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
	 *               {@link #REASON_VALIDATION}, {@link #REASON_CANCELLED}, {@link #REASON_NOT_ENABLED},
	 *               {@link #REASON_UNSUPPORTED_CONFIGURATION},
	 *               or {@link #REASON_REQUEST_FAILED} (for other reason).
	 */
	void onRequestFailed(@NonNull final BluetoothDevice device, final int status);
}
