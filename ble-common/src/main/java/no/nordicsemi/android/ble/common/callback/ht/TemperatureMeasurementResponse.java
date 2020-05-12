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
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

import no.nordicsemi.android.ble.common.profile.ht.TemperatureMeasurementCallback;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     TemperatureMeasurementResponse response = waitForNotification(characteristic)
 *           .awaitValid(TemperatureMeasurementResponse.class);
 *     float tempCelsius = response.getTemperatureCelsius());
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
public final class TemperatureMeasurementResponse extends TemperatureMeasurementDataCallback implements Parcelable {
	private float temperature;
	private int unit;
	@Nullable
	private Calendar timestamp;
	@Nullable
	private Integer type;

	public TemperatureMeasurementResponse() {
		// empty
	}

	@Override
	public void onTemperatureMeasurementReceived(@NonNull final BluetoothDevice device,
												 final float temperature, final int unit,
												 @Nullable final Calendar calendar,
												 @Nullable final Integer type) {
		this.temperature = temperature;
		this.unit = unit;
		this.timestamp = calendar;
		this.type = type;
	}

	public float getTemperature() {
		return temperature;
	}

	public float getTemperatureCelsius() {
		return TemperatureMeasurementCallback.toCelsius(temperature, unit);
	}

	public float getTemperatureFahrenheit() {
		return TemperatureMeasurementCallback.toFahrenheit(temperature, unit);
	}

	public int getUnit() {
		return unit;
	}

	@Nullable
	public Calendar getTimestamp() {
		return timestamp;
	}

	@Nullable
	public Integer getType() {
		return type;
	}

	// Parcelable
	@SuppressWarnings("ConstantConditions")
	private TemperatureMeasurementResponse(final Parcel in) {
		super(in);
		temperature = in.readFloat();
		unit = in.readInt();
		if (in.readByte() == 0) {
			timestamp = null;
		} else {
			timestamp = Calendar.getInstance();
			timestamp.setTimeInMillis(in.readLong());
		}
		if (in.readByte() == 0) {
			type = null;
		} else {
			type = in.readInt();
		}
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeFloat(temperature);
		dest.writeInt(unit);
		if (timestamp == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeLong(timestamp.getTimeInMillis());
		}
		if (type == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(type);
		}
	}

	public static final Creator<TemperatureMeasurementResponse> CREATOR = new Creator<TemperatureMeasurementResponse>() {
		@Override
		public TemperatureMeasurementResponse createFromParcel(final Parcel in) {
			return new TemperatureMeasurementResponse(in);
		}

		@Override
		public TemperatureMeasurementResponse[] newArray(final int size) {
			return new TemperatureMeasurementResponse[size];
		}
	};
}
