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

package no.nordicsemi.android.ble.common.callback.sc;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     SpeedAndCadenceControlPointResponse response = waitForIndication(characteristic)
 *           .trigger(writeCharacteristic(characteristic, SpeedAndCadenceControlPointData.requestSupportedSensorLocations()))
 *           .awaitValid(SpeedAndCadenceControlPointResponse.class);
 *     if (response.isOperationCompleted()) {
 *         int locations = response.getSupportedSensorLocations();
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
public final class SpeedAndCadenceControlPointResponse extends SpeedAndCadenceControlPointDataCallback implements Parcelable {
	private boolean operationCompleted;
	private int requestCode;
	private int errorCode;
	private int[] locations;

	public SpeedAndCadenceControlPointResponse() {
		// empty
	}

	@Override
	public void onSCOperationCompleted(@NonNull final BluetoothDevice device, final int requestCode) {
		this.operationCompleted = true;
		this.requestCode = requestCode;
	}

	@Override
	public void onSCOperationError(@NonNull final BluetoothDevice device, final int requestCode, final int errorCode) {
		this.operationCompleted = false;
		this.requestCode = requestCode;
		this.errorCode = errorCode;
	}

	@Override
	public void onSupportedSensorLocationsReceived(@NonNull final BluetoothDevice device, @NonNull final int[] locations) {
		this.operationCompleted = true;
		this.requestCode = SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS;
		this.locations = locations;
	}

	public boolean isOperationCompleted() {
		return operationCompleted;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	@Nullable
	public int[] getSupportedSensorLocations() {
		return locations;
	}

	// Parcelable
	private SpeedAndCadenceControlPointResponse(final Parcel in) {
		super(in);
		operationCompleted = in.readByte() != 0;
		requestCode = in.readInt();
		errorCode = in.readInt();
		locations = in.createIntArray();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeByte((byte) (operationCompleted ? 1 : 0));
		dest.writeInt(requestCode);
		dest.writeInt(errorCode);
		dest.writeIntArray(locations);
	}

	public static final Creator<SpeedAndCadenceControlPointResponse> CREATOR = new Creator<SpeedAndCadenceControlPointResponse>() {
		@Override
		public SpeedAndCadenceControlPointResponse createFromParcel(final Parcel in) {
			return new SpeedAndCadenceControlPointResponse(in);
		}

		@Override
		public SpeedAndCadenceControlPointResponse[] newArray(final int size) {
			return new SpeedAndCadenceControlPointResponse[size];
		}
	};
}
