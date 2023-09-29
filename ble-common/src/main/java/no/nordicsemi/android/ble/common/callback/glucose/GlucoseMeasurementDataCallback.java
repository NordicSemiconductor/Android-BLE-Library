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

package no.nordicsemi.android.ble.common.callback.glucose;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import androidx.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.callback.DateTimeDataCallback;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses value into Glucose Measurement data.
 * If the value received do not match required syntax
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * will be called.
 */
public abstract class GlucoseMeasurementDataCallback extends ProfileReadResponse implements GlucoseMeasurementCallback {

	public GlucoseMeasurementDataCallback() {
		// empty
	}

	protected GlucoseMeasurementDataCallback(final Parcel in) {
		super(in);
	}

	/** @noinspection DataFlowIssue*/
	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() < 10) {
			onInvalidDataReceived(device, data);
			return;
		}

		int offset = 0;

		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);
		final boolean timeOffsetPresent = (flags & 0x01) != 0;
		final boolean glucoseDataPresent = (flags & 0x02) != 0;
		final boolean unitMolL = (flags & 0x04) != 0;
		final boolean sensorStatusAnnunciationPresent = (flags & 0x08) != 0;
		final boolean contextInformationFollows = (flags & 0x10) != 0;

		if (data.size() < 10 + (timeOffsetPresent ? 2 : 0) + (glucoseDataPresent ? 3 : 0)
			+ (sensorStatusAnnunciationPresent ? 2 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Required fields
		final int sequenceNumber = data.getIntValue(Data.FORMAT_UINT16_LE, offset);
		offset += 2;
		final Calendar baseTime = DateTimeDataCallback.readDateTime(data, 3);
		offset += 7;

		if (baseTime == null) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Optional fields
		if (timeOffsetPresent) {
			final int timeOffset = data.getIntValue(Data.FORMAT_SINT16_LE, offset);
			offset += 2;

			baseTime.add(Calendar.MINUTE, timeOffset);
		}

		Float glucoseConcentration = null;
		Integer unit = null;
		Integer type = null;
		Integer sampleLocation = null;
		if (glucoseDataPresent) {
			glucoseConcentration = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			final int typeAndSampleLocation = data.getIntValue(Data.FORMAT_UINT8, offset + 2);
			offset += 3;

			type = typeAndSampleLocation & 0x0F;
			sampleLocation = typeAndSampleLocation >> 4;
			unit = unitMolL ? UNIT_mol_L : UNIT_kg_L;
		}

		GlucoseStatus status = null;
		if (sensorStatusAnnunciationPresent) {
			final int value = data.getIntValue(Data.FORMAT_UINT16_LE, offset);
			// offset += 2;

			status = new GlucoseStatus(value);
		}

		onGlucoseMeasurementReceived(device, sequenceNumber, baseTime /* with offset */,
				glucoseConcentration, unit, type, sampleLocation, status, contextInformationFollows);
	}
}
