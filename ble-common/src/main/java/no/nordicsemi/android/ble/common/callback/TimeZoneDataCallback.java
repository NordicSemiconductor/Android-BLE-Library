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

package no.nordicsemi.android.ble.common.callback;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.TimeZoneCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses 1-byte value into a Time Zone offset.
 * If the value received is shorter than 1 byte the
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.time_zone.xml
 */
@SuppressWarnings("WeakerAccess")
public abstract class TimeZoneDataCallback extends ProfileReadResponse implements TimeZoneCallback {
	public TimeZoneDataCallback() {
		// empty
	}

	protected TimeZoneDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		final Integer offset = readTimeZone(data, 0);
		if (offset == null) {
			onInvalidDataReceived(device, data);
			return;
		}

		if (offset == -128) {
			onUnknownTimeZoneReceived(device);
		} else if (offset < -48 || offset > 56) {
			onInvalidDataReceived(device, data);
		} else {
			onTimeZoneReceived(device, offset * 15);
		}
	}

	/**
	 * Offset from UTC in number of 15 minutes increments. A value of -128 means that the time zone
	 * offset is not known. The offset defined in this characteristic is constant, regardless
	 * whether daylight savings is in effect.
	 *
	 * @param data   data received.
	 * @param offset offset from which the time zone is to be read.
	 * @return the time offset in 15 minutes increments, or null if offset is outside ot range.
	 */
	@Nullable
	public static Integer readTimeZone(@NonNull final Data data, final int offset) {
		if (data.size() < offset + 1)
			return null;

		return data.getIntValue(Data.FORMAT_SINT8, offset);
	}
}
