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
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     GlucoseMeasurementResponse response = waitForNotification(characteristic)
 *           .awaitValid(GlucoseMeasurementResponse.class);
 *     if (response.contextInformationFollows()) {
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
public final class GlucoseMeasurementResponse extends GlucoseMeasurementDataCallback implements Parcelable {
	private int sequenceNumber;
	@Nullable
	private Calendar time;
	@Nullable
	private Float glucoseConcentration;
	@Nullable
	private Integer unit;
	@Nullable
	private Integer type;
	@Nullable
	private Integer sampleLocation;
	@Nullable
	private GlucoseStatus status;
	private boolean contextInformationFollows;

	@Override
	public void onGlucoseMeasurementReceived(@NonNull final BluetoothDevice device,
											 final int sequenceNumber,
											 @NonNull final Calendar time,
											 @Nullable final Float glucoseConcentration,
											 @Nullable final Integer unit,
											 @Nullable final Integer type,
											 @Nullable final Integer sampleLocation,
											 @Nullable final GlucoseStatus status,
											 final boolean contextInformationFollows) {
		this.sequenceNumber = sequenceNumber;
		this.time = time;
		this.glucoseConcentration = glucoseConcentration;
		this.unit = unit;
		this.type = type;
		this.sampleLocation = sampleLocation;
		this.status = status;
		this.contextInformationFollows = contextInformationFollows;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	@Nullable
	public Calendar getTime() {
		return time;
	}

	@Nullable
	public Float getGlucoseConcentration() {
		return glucoseConcentration;
	}

	@Nullable
	public Integer getUnit() {
		return unit;
	}

	@Nullable
	public Integer getType() {
		return type;
	}

	@Nullable
	public Integer getSampleLocation() {
		return sampleLocation;
	}

	@Nullable
	public GlucoseStatus getStatus() {
		return status;
	}

	public boolean contextInformationFollows() {
		return contextInformationFollows;
	}

	// Parcelable
	@SuppressWarnings("ConstantConditions")
	private GlucoseMeasurementResponse(final Parcel in) {
		super(in);
		sequenceNumber = in.readInt();
		if (in.readByte() == 0) {
			time = null;
		} else {
			time = Calendar.getInstance();
			time.setTimeInMillis(in.readLong());
		}
		if (in.readByte() == 0) {
			glucoseConcentration = null;
		} else {
			glucoseConcentration = in.readFloat();
		}
		if (in.readByte() == 0) {
			unit = null;
		} else {
			unit = in.readInt();
		}
		if (in.readByte() == 0) {
			type = null;
		} else {
			type = in.readInt();
		}
		if (in.readByte() == 0) {
			sampleLocation = null;
		} else {
			sampleLocation = in.readInt();
		}
		if (in.readByte() == 0) {
			status = null;
		} else {
			status = new GlucoseStatus(in.readInt());
		}
		contextInformationFollows = in.readByte() != 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(sequenceNumber);
		if (time == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeLong(time.getTimeInMillis());
		}
		if (glucoseConcentration == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeFloat(glucoseConcentration);
		}
		if (unit == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(unit);
		}
		if (type == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(type);
		}
		if (sampleLocation == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(sampleLocation);
		}
		super.writeToParcel(dest, flags);
		if (status == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(status.value);
		}
		dest.writeByte((byte) (contextInformationFollows ? 1 : 0));
	}

	public static final Creator<GlucoseMeasurementResponse> CREATOR = new Creator<GlucoseMeasurementResponse>() {
		@Override
		public GlucoseMeasurementResponse createFromParcel(final Parcel in) {
			return new GlucoseMeasurementResponse(in);
		}

		@Override
		public GlucoseMeasurementResponse[] newArray(final int size) {
			return new GlucoseMeasurementResponse[size];
		}
	};
}
