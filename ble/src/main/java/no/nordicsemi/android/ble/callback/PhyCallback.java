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
import no.nordicsemi.android.ble.annotation.PhyValue;

@SuppressWarnings("unused")
public interface PhyCallback {
	/**
	 * Bluetooth LE 1M PHY. Used to refer to LE 1M Physical Channel for advertising, scanning or
	 * connection.
	 */
	int PHY_LE_1M = 1;

	/**
	 * Bluetooth LE 2M PHY. Used to refer to LE 2M Physical Channel for advertising, scanning or
	 * connection.
	 */
	int PHY_LE_2M = 2;

	/**
	 * Bluetooth LE Coded PHY. Used to refer to LE Coded Physical Channel for advertising, scanning
	 * or connection.
	 */
	int PHY_LE_CODED = 3;

	/**
	 * Method called when the PHY value has changed or was read.
	 *
	 * @param device the target device.
	 * @param txPhy the transmitter PHY in use. One of {@link #PHY_LE_1M},
	 *             {@link #PHY_LE_2M}, and {@link #PHY_LE_CODED}.
	 * @param rxPhy the receiver PHY in use. One of {@link #PHY_LE_1M},
	 *             {@link #PHY_LE_2M}, and {@link #PHY_LE_CODED}.
	 */
	void onPhyChanged(@NonNull final BluetoothDevice device,
					  @PhyValue final int txPhy, @PhyValue final int rxPhy);
}
