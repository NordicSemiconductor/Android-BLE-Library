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

/**
 * The connection parameters for a Bluetooth LE connection is a set of parameters that determine
 * when and how the Central and a Peripheral in a link transmits data.
 * It is always the Central that actually sets the connection parameters used, but the Peripheral
 * can send a so-called Connection Parameter Update Request, that the Central can then accept or reject.
 * <p>
 * On Android, requesting connection parameters is available since Android Lollipop using
 * {@link android.bluetooth.BluetoothGatt#requestConnectionPriority(int)}. There are 3 options
 * available: {@link android.bluetooth.BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER},
 * {@link android.bluetooth.BluetoothGatt#CONNECTION_PRIORITY_BALANCED} and
 * {@link android.bluetooth.BluetoothGatt#CONNECTION_PRIORITY_HIGH}. See
 * {@link no.nordicsemi.android.ble.Request#newConnectionPriorityRequest(int)} for details.
 * <p>
 * Until Android 8.0 Oreo, there was no callback indicating whether the change has succeeded,
 * or not. Also, when a Central or Peripheral requested connection parameters change without
 * explicit calling of this method, the application was not aware of it.
 * Android Oreo added a hidden callback to {@link android.bluetooth.BluetoothGattCallback}
 * notifying about connection parameters change. Those values will be reported with this callback.
 *
 * @deprecated Use {@link ConnectionParametersUpdatedCallback} instead.
 */
@Deprecated
@FunctionalInterface
public interface ConnectionPriorityCallback extends ConnectionParametersUpdatedCallback {
	// No extra methods.
}
