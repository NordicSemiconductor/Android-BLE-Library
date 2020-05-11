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

package no.nordicsemi.android.ble.common.profile.cgm;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.data.Data;

public interface CGMSessionStartTimeCallback {

	/**
	 * Callback called whenever the CGM Session Start Time characteristic was read.
	 * <p>
	 * If the E2E CRC field was present in the CGM packet, the data has been verified against it.
	 * If CRC check has failed, the
	 * {@link #onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError(BluetoothDevice, Data)}
	 * will be called instead.
	 *
	 * @param device   the target device.
	 * @param calendar the date and time received, as {@link Calendar} object.
	 *                 Time zone and DST offset are included in the calendar.
	 * @param secured  true if the packet was sent with E2E-CRC value that was verified to
	 *                 match the packet, false if the packet didn't contain CRC field.
	 *                 For a callback in case of invalid CRC value check
	 *                 {@link #onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError(BluetoothDevice, Data)}.
	 */
	void onContinuousGlucoseMonitorSessionStartTimeReceived(
			@NonNull final BluetoothDevice device,
			@NonNull final Calendar calendar,
			final boolean secured);

	/**
	 * Callback called when a CGM Session Start Time packet with E2E field was received but the
	 * CRC check has failed.
	 *
	 * @param device the target device.
	 * @param data   the CGM Session Start Time  packet data that was received, including the
	 *               CRC field.
	 */
	default void onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError(
			@NonNull final BluetoothDevice device,
			@NonNull final Data data) {
		// ignore
	}
}
