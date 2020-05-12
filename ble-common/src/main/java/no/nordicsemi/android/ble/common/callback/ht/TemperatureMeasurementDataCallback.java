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

package no.nordicsemi.android.ble.common.callback.ht;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import androidx.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.callback.DateTimeDataCallback;
import no.nordicsemi.android.ble.common.profile.ht.TemperatureMeasurementCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses value into Temperature Measurement data.
 * If the value received do not match required syntax
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.temperature_measurement.xml
 */
@SuppressWarnings({"unused", "WeakerAccess", "ConstantConditions"})
public abstract class TemperatureMeasurementDataCallback extends ProfileReadResponse implements TemperatureMeasurementCallback {

	public TemperatureMeasurementDataCallback() {
		// empty
	}

	protected TemperatureMeasurementDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() < 5) {
			onInvalidDataReceived(device, data);
			return;
		}

		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset);
		final int unit = (flags & 0x01) == UNIT_C ? UNIT_C : UNIT_F;
		final boolean timestampPresent = (flags & 0x02) != 0;
		final boolean temperatureTypePresent = (flags & 0x04) != 0;
		offset += 1;

		if (data.size() < 5 + (timestampPresent ? 7 : 0) + (temperatureTypePresent ? 1 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		final float temperature = data.getFloatValue(Data.FORMAT_FLOAT, 1);
		offset += 4;

		Calendar calendar = null;
		if (timestampPresent) {
			calendar = DateTimeDataCallback.readDateTime(data, offset);
			offset += 7;
		}

		Integer type = null;
		if (temperatureTypePresent) {
			type = data.getIntValue(Data.FORMAT_UINT8, offset);
			// offset += 1;
		}

		onTemperatureMeasurementReceived(device, temperature, unit, calendar, type);
	}
}
