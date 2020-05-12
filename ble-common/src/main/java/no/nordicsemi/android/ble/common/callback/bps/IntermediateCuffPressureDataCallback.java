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

package no.nordicsemi.android.ble.common.callback.bps;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import androidx.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.callback.DateTimeDataCallback;
import no.nordicsemi.android.ble.common.profile.bp.IntermediateCuffPressureCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses value into Intermediate Cuff Pressure data.
 * If the value received do not match required syntax
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.intermediate_cuff_pressure.xml
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public abstract class IntermediateCuffPressureDataCallback extends ProfileReadResponse implements IntermediateCuffPressureCallback {

	public IntermediateCuffPressureDataCallback() {
		// empty
	}

	protected IntermediateCuffPressureDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() < 7) {
			onInvalidDataReceived(device, data);
			return;
		}
		// First byte: flags
		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);

		// See UNIT_* for unit options
		final int unit = (flags & 0x01) == UNIT_mmHg ? UNIT_mmHg : UNIT_kPa;
		final boolean timestampPresent         = (flags & 0x02) != 0;
		final boolean pulseRatePresent         = (flags & 0x04) != 0;
		final boolean userIdPresent            = (flags & 0x08) != 0;
		final boolean measurementStatusPresent = (flags & 0x10) != 0;

		if (data.size() < 7
				+ (timestampPresent ? 7 : 0) + (pulseRatePresent ? 2 : 0)
				+ (userIdPresent ? 1 : 0) + (measurementStatusPresent ? 2 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Following bytes - systolic, diastolic and mean arterial pressure
		final float cuffPressure = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
		// final float ignored_1 = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 2);
		// final float ignored_2 = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 4);
		offset += 6;

		// Parse timestamp if present
		Calendar calendar = null;
		if (timestampPresent) {
			calendar = DateTimeDataCallback.readDateTime(data, offset);
			offset += 7;
		}

		// Parse pulse rate if present
		Float pulseRate = null;
		if (pulseRatePresent) {
			pulseRate = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			offset += 2;
		}

		// Read user id if present
		Integer userId = null;
		if (userIdPresent) {
			userId = data.getIntValue(Data.FORMAT_UINT8, offset);
			offset += 1;
		}

		// Read measurement status if present
		BPMStatus status = null;
		if (measurementStatusPresent) {
			final int measurementStatus = data.getIntValue(Data.FORMAT_UINT16, offset);
			// offset += 2;
			status = new BPMStatus(measurementStatus);
		}

		onIntermediateCuffPressureReceived(device, cuffPressure, unit, pulseRate, userId, status, calendar);
	}
}
