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

package no.nordicsemi.android.ble.common.callback.cgm;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.callback.DSTOffsetDataCallback;
import no.nordicsemi.android.ble.common.callback.DateTimeDataCallback;
import no.nordicsemi.android.ble.common.callback.TimeZoneDataCallback;
import no.nordicsemi.android.ble.common.profile.cgm.CGMSessionStartTimeCallback;
import no.nordicsemi.android.ble.common.profile.DSTOffsetCallback;
import no.nordicsemi.android.ble.common.util.CRC16;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses value into CGM Session Start Time data.
 * If the value received do not match required syntax
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * If the device supports E2E CRC validation and the CRC is not valid, the
 * {@link #onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError(BluetoothDevice, Data)}
 * will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.cgm_session_start_time.xml
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public abstract class CGMSessionStartTimeDataCallback extends ProfileReadResponse implements CGMSessionStartTimeCallback {

	public CGMSessionStartTimeDataCallback() {
		// empty
	}

	protected CGMSessionStartTimeDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() != 9 && data.size() != 11) {
			onInvalidDataReceived(device, data);
			return;
		}

		final boolean crcPresent = data.size() == 11;
		if (crcPresent) {
			final int actualCrc = CRC16.MCRF4XX(data.getValue(), 0, 9);
			final int expectedCrc = data.getIntValue(Data.FORMAT_UINT16, 9);
			if (actualCrc != expectedCrc) {
				onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError(device, data);
				return;
			}
		}

		final Calendar calendar = DateTimeDataCallback.readDateTime(data, 0);
		final Integer timeZoneOffset = TimeZoneDataCallback.readTimeZone(data, 7); // [minutes]
		final DSTOffsetCallback.DSTOffset dstOffset = DSTOffsetDataCallback.readDSTOffset(data, 8);

		if (calendar == null || timeZoneOffset == null || dstOffset == null) {
			onInvalidDataReceived(device, data);
			return;
		}

		final TimeZone timeZone = new TimeZone() {
			@Override
			public int getOffset(final int era, final int year, final int month, final int day, final int dayOfWeek, final int milliseconds) {
				return (timeZoneOffset + dstOffset.offset) * 60000; // convert minutes to milliseconds
			}

			@Override
			public void setRawOffset(final int offsetMillis) {
				throw new UnsupportedOperationException("Can't set raw offset for this TimeZone");
			}

			@Override
			public int getRawOffset() {
				return timeZoneOffset * 60000;
			}

			@Override
			public boolean useDaylightTime() {
				return true;
			}

			@Override
			public boolean inDaylightTime(final Date date) {
				// Use of DST is dependent on the input data only
				return dstOffset.offset > 0;
			}

			@Override
			public int getDSTSavings() {
				return dstOffset.offset * 60000;
			}

			// TODO add TimeZone ID
//			@Override
//			public String getID() {
//				return super.getID();
//			}
		};

		calendar.setTimeZone(timeZone);

		onContinuousGlucoseMonitorSessionStartTimeReceived(device, calendar, crcPresent);
	}
}
