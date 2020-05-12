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

package no.nordicsemi.android.ble.common.callback.rsc;

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
 *     RunningSpeedAndCadenceMeasurementResponse response = waitForIndication(characteristic)
 *           .awaitValid(RunningSpeedAndCadenceMeasurementResponse.class);
 *     float speedMetersPerSecond = response.getInstantaneousSpeed();
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
public final class RunningSpeedAndCadenceMeasurementResponse extends RunningSpeedAndCadenceMeasurementDataCallback
		implements Parcelable {
	private boolean running;
	private float instantaneousSpeed;
	private int instantaneousCadence;
	private Integer strideLength;
	private Long totalDistance;

	public RunningSpeedAndCadenceMeasurementResponse() {
		// empty
	}

	@Override
	public void onRSCMeasurementReceived(@NonNull final BluetoothDevice device, final boolean running,
										 final float instantaneousSpeed, final int instantaneousCadence,
										 @Nullable final Integer strideLength,
										 @Nullable final Long totalDistance) {
		this.running = running;
		this.instantaneousSpeed = instantaneousSpeed;
		this.instantaneousCadence = instantaneousCadence;
		this.strideLength = strideLength;
		this.totalDistance = totalDistance;
	}

	/**
	 * True if running has been detected. False otherwise (walking, or activity status not supported).
	 *
	 * @return True if user is running, false otherwise.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Returns instantaneous speed.
	 *
	 * @return Speed in meters per second.
	 */
	public float getInstantaneousSpeed() {
		return instantaneousSpeed;
	}

	/**
	 * Returns instantaneous cadence in revolution per minute (RPM).
	 *
	 * @return instantaneous cadence in 1/minute unit (RPM).
	 */
	public int getInstantaneousCadence() {
		return instantaneousCadence;
	}

	/**
	 * Returns the stride length in centimeters if such data was present in the packet.
	 *
	 * @return Stride length in centimeters or null, if data not available.
	 */
	@Nullable
	public Integer getStrideLength() {
		return strideLength;
	}

	/**
	 * Returns total distance traveled with the device, in meters. This value is returned as
	 * Long as the type returned is UINT32, and may be greater than Integer range.
	 *
	 * @return Total distance, in meters.
	 */
	@Nullable
	public Long getTotalDistance() {
		return totalDistance;
	}

	// Parcelable
	private RunningSpeedAndCadenceMeasurementResponse(final Parcel in) {
		super(in);
		running = in.readByte() != 0;
		instantaneousSpeed = in.readFloat();
		instantaneousCadence = in.readInt();
		if (in.readByte() == 0) {
			strideLength = null;
		} else {
			strideLength = in.readInt();
		}
		if (in.readByte() == 0) {
			totalDistance = null;
		} else {
			totalDistance = in.readLong();
		}
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeByte((byte) (running ? 1 : 0));
		dest.writeFloat(instantaneousSpeed);
		dest.writeInt(instantaneousCadence);
		if (strideLength == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(strideLength);
		}
		if (totalDistance == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeLong(totalDistance);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<RunningSpeedAndCadenceMeasurementResponse> CREATOR = new Creator<RunningSpeedAndCadenceMeasurementResponse>() {
		@Override
		public RunningSpeedAndCadenceMeasurementResponse createFromParcel(final Parcel in) {
			return new RunningSpeedAndCadenceMeasurementResponse(in);
		}

		@Override
		public RunningSpeedAndCadenceMeasurementResponse[] newArray(final int size) {
			return new RunningSpeedAndCadenceMeasurementResponse[size];
		}
	};
}
