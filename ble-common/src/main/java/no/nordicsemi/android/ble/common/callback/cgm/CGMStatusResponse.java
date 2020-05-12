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
import androidx.annotation.Nullable;

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
 *     CGMStatusResponse response = readCharacteristic(characteristic)
 *           .awaitValid(CGMStatusResponse.class);
 *     CGMStatus status = response.getStatus();
 *     if (status != null) {
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
public final class CGMStatusResponse extends CGMStatusDataCallback implements CRCSecuredResponse, Parcelable {
	@Nullable
	private CGMStatus status;
	private int timeOffset;
	private boolean secured;
	private boolean crcValid;

	public CGMStatusResponse() {
		// empty
	}

	@Override
	public void onContinuousGlucoseMonitorStatusChanged(@NonNull final BluetoothDevice device, @NonNull final CGMStatus status, final int timeOffset, final boolean secured) {
		this.status = status;
		this.timeOffset = timeOffset;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseMonitorStatusReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		onInvalidDataReceived(device, data);
		this.secured = true;
		this.crcValid = false;
	}

	@Nullable
	public CGMStatus getStatus() {
		return status;
	}

	public int getTimeOffset() {
		return timeOffset;
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
	private CGMStatusResponse(final Parcel in) {
		super(in);
		timeOffset = in.readInt();
		if (in.readByte() == 0) {
			status = null;
		} else {
			final int warningStatus = in.readInt();
			final int calibrationTempStatus = in.readInt();
			final int sensorStatus = in.readInt();
			status = new CGMStatus(warningStatus, calibrationTempStatus, sensorStatus);
		}
		secured = in.readByte() != 0;
		crcValid = in.readByte() != 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(timeOffset);
		if (status == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(status.warningStatus);
			dest.writeInt(status.calibrationTempStatus);
			dest.writeInt(status.sensorStatus);
		}
		dest.writeByte((byte) (secured ? 1 : 0));
		dest.writeByte((byte) (crcValid ? 1 : 0));
	}

	public static final Creator<CGMStatusResponse> CREATOR = new Creator<CGMStatusResponse>() {
		@Override
		public CGMStatusResponse createFromParcel(final Parcel in) {
			return new CGMStatusResponse(in);
		}

		@Override
		public CGMStatusResponse[] newArray(final int size) {
			return new CGMStatusResponse[size];
		}
	};
}
