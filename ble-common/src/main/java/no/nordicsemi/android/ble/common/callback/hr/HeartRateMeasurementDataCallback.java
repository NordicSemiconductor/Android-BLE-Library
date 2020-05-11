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

package no.nordicsemi.android.ble.common.callback.hr;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.hr.HeartRateMeasurementCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses value into Heart Rate Measurement data.
 * If the value received do not match required syntax
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.heart_rate_measurement.xml
 */
@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
public abstract class HeartRateMeasurementDataCallback extends ProfileReadResponse implements HeartRateMeasurementCallback {

	public HeartRateMeasurementDataCallback() {
		// empty
	}

	protected HeartRateMeasurementDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() < 2) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Read flags
		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset);
		final int hearRateType = (flags & 0x01) == 0 ? Data.FORMAT_UINT8 : Data.FORMAT_UINT16;
		final int sensorContactStatus = (flags & 0x06) >> 1;
		final boolean sensorContactSupported = sensorContactStatus == 2 || sensorContactStatus == 3;
		final boolean sensorContactDetected = sensorContactStatus == 3;
		final boolean energyExpandedPresent = (flags & 0x08) != 0;
		final boolean rrIntervalsPresent = (flags & 0x10) != 0;
		offset += 1;

		// Validate packet length
		if (data.size() < 1 + (hearRateType & 0x0F)
				+ (energyExpandedPresent ? 2 : 0)
				+ (rrIntervalsPresent ? 2 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Prepare data
		final Boolean sensorContact = sensorContactSupported ? sensorContactDetected : null;

		final int heartRate = data.getIntValue(hearRateType, offset);
		offset += hearRateType & 0xF;

		Integer energyExpanded = null;
		if (energyExpandedPresent) {
			energyExpanded = data.getIntValue(Data.FORMAT_UINT16, offset);
			offset += 2;
		}

		List<Integer> rrIntervals = null;
		if (rrIntervalsPresent) {
			final int count = (data.size() - offset) / 2;
			final List<Integer> intervals = new ArrayList<>(count);
			for (int i = 0; i < count; ++i) {
				intervals.add(data.getIntValue(Data.FORMAT_UINT16, offset));
				offset += 2;
			}
			rrIntervals = Collections.unmodifiableList(intervals);
		}

		onHeartRateMeasurementReceived(device, heartRate, sensorContact, energyExpanded, rrIntervals);
	}
}
