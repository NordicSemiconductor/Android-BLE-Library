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

package no.nordicsemi.android.ble.common.callback.bps;

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
 *     BloodPressureMeasurementResponse response = readCharacteristic(characteristic)
 *           .awaitValid(BloodPressureMeasurementResponse.class);
 *     if (response.getStatus() != null) {
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
public final class BloodPressureMeasurementResponse extends BloodPressureMeasurementDataCallback implements Parcelable {
	private float systolic;
	private float diastolic;
	private float meanArterialPressure;
	private int unit;
	private Float pulseRate;
	private Integer userID;
	private BPMStatus status;
	private Calendar calendar;

	public BloodPressureMeasurementResponse() {
		// empty
	}

	@Override
	public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
												   final float systolic, final float diastolic,
												   final float meanArterialPressure, final int unit,
												   @Nullable final Float pulseRate,
												   @Nullable final Integer userID,
												   @Nullable final BPMStatus status,
												   @Nullable final Calendar calendar) {
		this.systolic = systolic;
		this.diastolic = diastolic;
		this.meanArterialPressure = meanArterialPressure;
		this.unit = unit;
		this.pulseRate = pulseRate;
		this.userID = userID;
		this.status = status;
		this.calendar = calendar;
	}

	public float getSystolic() {
		return systolic;
	}

	public float getDiastolic() {
		return diastolic;
	}

	public float getMeanArterialPressure() {
		return meanArterialPressure;
	}

	/**
	 * Returns the measurement unit, one of {@link #UNIT_mmHg} or {@link #UNIT_kPa}.
	 * To convert to proper unit, use {@link #toMmHg(float, int)} or {@link #toKPa(float, int)}.
	 *
	 * @return Unit of systolic, diastolic and mean arterial pressure.
	 */
	public int getUnit() {
		return unit;
	}

	@Nullable
	public Float getPulseRate() {
		return pulseRate;
	}

	@Nullable
	public Integer getUserID() {
		return userID;
	}

	@Nullable
	public BPMStatus getStatus() {
		return status;
	}

	@Nullable
	public Calendar getTimestamp() {
		return calendar;
	}

	// Parcelable
	private BloodPressureMeasurementResponse(final Parcel in) {
		super(in);
		systolic = in.readFloat();
		diastolic = in.readFloat();
		meanArterialPressure = in.readFloat();
		unit = in.readInt();
		if (in.readByte() == 0) {
			pulseRate = null;
		} else {
			pulseRate = in.readFloat();
		}
		if (in.readByte() == 0) {
			userID = null;
		} else {
			userID = in.readInt();
		}
		if (in.readByte() == 0) {
			status = null;
		} else {
			status = new BPMStatus(in.readInt());
		}
		if (in.readByte() == 0) {
			calendar = null;
		} else {
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(in.readLong());
		}
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeFloat(systolic);
		dest.writeFloat(diastolic);
		dest.writeFloat(meanArterialPressure);
		dest.writeInt(unit);
		if (pulseRate == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeFloat(pulseRate);
		}
		if (userID == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(userID);
		}
		if (status == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(status.value);
		}
		if (calendar == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeLong(calendar.getTimeInMillis());
		}
	}

	public static final Creator<BloodPressureMeasurementResponse> CREATOR = new Creator<BloodPressureMeasurementResponse>() {
		@Override
		public BloodPressureMeasurementResponse createFromParcel(final Parcel in) {
			return new BloodPressureMeasurementResponse(in);
		}

		@Override
		public BloodPressureMeasurementResponse[] newArray(final int size) {
			return new BloodPressureMeasurementResponse[size];
		}
	};
}
