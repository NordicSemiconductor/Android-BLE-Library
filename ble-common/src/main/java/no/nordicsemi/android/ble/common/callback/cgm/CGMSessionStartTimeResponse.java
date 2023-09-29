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
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     CGMSessionStartTimeResponse response = readCharacteristic(characteristic)
 *           .awaitValid(CGMSessionStartTimeResponse.class);
 *    Calendar startTime = response.getStartTime();
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
public final class CGMSessionStartTimeResponse extends CGMSessionStartTimeDataCallback implements CRCSecuredResponse, Parcelable {
	private Calendar startTime;
	private boolean secured;
	private boolean crcValid;

	public CGMSessionStartTimeResponse() {
		// empty
	}

	@Override
	public void onContinuousGlucoseMonitorSessionStartTimeReceived(@NonNull final BluetoothDevice device,
																   @NonNull final Calendar calendar, final boolean secured) {
		this.startTime = calendar;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError(@NonNull final BluetoothDevice device,
																			   @NonNull final Data data) {
		onInvalidDataReceived(device, data);
		this.secured = true;
		this.crcValid = false;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	@Override
	public boolean isSecured() {
		return secured;
	}

	@Override
	public boolean isCrcValid() {
		return crcValid;
	}

	// Parcelable
	private CGMSessionStartTimeResponse(final Parcel in) {
		super(in);
		if (in.readByte() == 0) {
			startTime = null;
		} else {
			startTime = Calendar.getInstance();
			startTime.setTimeInMillis(in.readLong());
		}
		secured = in.readByte() != 0;
		crcValid = in.readByte() != 0;
	}

	@Override
	public void writeToParcel(@NonNull final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		if (startTime == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeLong(startTime.getTimeInMillis());
		}
		dest.writeByte((byte) (secured ? 1 : 0));
		dest.writeByte((byte) (crcValid ? 1 : 0));
	}

	public static final Creator<CGMSessionStartTimeResponse> CREATOR = new Creator<>() {
		@Override
		public CGMSessionStartTimeResponse createFromParcel(final Parcel in) {
			return new CGMSessionStartTimeResponse(in);
		}

		@Override
		public CGMSessionStartTimeResponse[] newArray(final int size) {
			return new CGMSessionStartTimeResponse[size];
		}
	};
}
