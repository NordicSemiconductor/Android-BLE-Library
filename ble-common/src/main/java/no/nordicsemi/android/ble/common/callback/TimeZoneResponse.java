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
import android.os.Parcelable;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     TimeZoneResponse response = readCharacteristic(characteristic)
 *           .awaitValid(TimeZoneResponse.class);
 *     if (response.isTimeZoneOffsetKnown()) {
 *         int offset = response.getTimeZoneOffset();
 *         ...
 *     }
 *     ...
 * } catch ({@link RequestFailedException} e) {
 *     Log.w(TAG, "Request failed with status " + e.getStatus(), e);
 * } catch ({@link InvalidDataException} e) {
 *     Log.w(TAG, "Invalid data received: " + e.getResponse().getRawData());
 * }
 * </pre>
 * </p>
 */
@SuppressWarnings("unused")
public final class TimeZoneResponse extends TimeZoneDataCallback implements Parcelable {
	private int timeZoneOffset;
	private boolean timeZoneOffsetKnown;

	public TimeZoneResponse() {
		// empty
	}

	@Override
	public void onTimeZoneReceived(@NonNull final BluetoothDevice device, final int offset) {
		this.timeZoneOffset = offset;
		this.timeZoneOffsetKnown = true;
	}

	@Override
	public void onUnknownTimeZoneReceived(@NonNull final BluetoothDevice device) {
		this.timeZoneOffsetKnown = false;
	}

	public int getTimeZoneOffset() {
		return timeZoneOffset;
	}

	public boolean isTimeZoneOffsetKnown() {
		return timeZoneOffsetKnown;
	}

	// Parcelable
	private TimeZoneResponse(final Parcel in) {
		super(in);
		timeZoneOffset = in.readInt();
		timeZoneOffsetKnown = in.readByte() != 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(timeZoneOffset);
		dest.writeByte((byte) (timeZoneOffsetKnown ? 1 : 0));
	}

	public static final Creator<TimeZoneResponse> CREATOR = new Creator<TimeZoneResponse>() {
		@Override
		public TimeZoneResponse createFromParcel(final Parcel in) {
			return new TimeZoneResponse(in);
		}

		@Override
		public TimeZoneResponse[] newArray(final int size) {
			return new TimeZoneResponse[size];
		}
	};
}
