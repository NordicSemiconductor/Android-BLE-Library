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

package no.nordicsemi.android.ble.common.callback.csc;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     CyclingSpeedAndCadenceResponse response = waitForNotification(characteristic)
 *           .awaitValid(CyclingSpeedAndCadenceResponse.class);
 *     float totalDistance = response.getTotalDistance(2340.0f);
 *     ...
 * } catch ({@link RequestFailedException} e) {
 *     Log.w(TAG, "Request failed with status " + e.getStatus(), e);
 * } catch ({@link InvalidDataException} e) {
 *     Log.w(TAG, "Invalid data received: " + e.getResponse().getRawData());
 * }
 * </pre>
 * </p>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class CyclingSpeedAndCadenceMeasurementResponse extends CyclingSpeedAndCadenceMeasurementDataCallback implements Parcelable {
	private long wheelRevolutions;
	private long crankRevolutions;
	private int lastWheelEventTime;
	private int lastCrankEventTime;

	public CyclingSpeedAndCadenceMeasurementResponse() {
		// empty
	}

	@Override
	public void onWheelMeasurementReceived(@NonNull final BluetoothDevice device, final long wheelRevolutions, final int lastWheelEventTime) {
		this.wheelRevolutions = wheelRevolutions;
		this.lastWheelEventTime = lastWheelEventTime;
	}

	@Override
	public void onCrankMeasurementReceived(@NonNull final BluetoothDevice device, final int crankRevolutions, final int lastCrankEventTime) {
		this.crankRevolutions = crankRevolutions;
		this.lastCrankEventTime = lastCrankEventTime;
	}

	@Override
	public void onDistanceChanged(@NonNull final BluetoothDevice device, final float totalDistance, final float distance, final float speed) {
		// never called, as the wheel circumference is not known here
	}

	@Override
	public void onCrankDataChanged(@NonNull final BluetoothDevice device, final float crankCadence, final float gearRatio) {
		// never called
	}

	public long getWheelRevolutions() {
		return wheelRevolutions;
	}

	public long getCrankRevolutions() {
		return crankRevolutions;
	}

	public long getLastWheelEventTime() {
		return lastWheelEventTime;
	}

	public long getLastCrankEventTime() {
		return lastCrankEventTime;
	}

	/**
	 * Returns the total distance since the device was reset.
	 *
	 * @param wheelCircumference the wheel circumference in millimeters.
	 * @return total distance traveled, in meters.
	 */
	public float getTotalDistance(final float wheelCircumference) {
		return (float) wheelRevolutions * wheelCircumference / 1000.0f; // [m]
	}

	/**
	 * Returns the distance traveled since the given response was received.
	 *
	 * @param wheelCircumference the wheel circumference in millimeters.
	 * @param previous a previous response.
	 * @return distance traveled since the previous response, in meters.
	 */
	public float getDistance(final float wheelCircumference,
							 final CyclingSpeedAndCadenceMeasurementResponse previous) {
		return (float) (wheelRevolutions - previous.wheelRevolutions) * wheelCircumference / 1000.0f; // [m]
	}

	/**
	 * Returns the average speed since the previous response was received.
	 *
	 * @param wheelCircumference the wheel circumference in millimeters.
	 * @param previous a previous response.
	 * @return speed in meters per second.
	 */
	public float getSpeed(final float wheelCircumference,
						  final CyclingSpeedAndCadenceMeasurementResponse previous) {
		float timeDifference;
		if (lastWheelEventTime < previous.lastWheelEventTime)
			timeDifference = (65535 + lastWheelEventTime - previous.lastWheelEventTime) / 1024.0f; // [s]
		else
			timeDifference = (lastWheelEventTime - previous.lastWheelEventTime) / 1024.0f; // [s]

		return getDistance(wheelCircumference, previous) / timeDifference; // [m/s]
	}

	/**
	 * Returns average wheel cadence since the previous message was received.
	 *
	 * @param previous a previous response.
	 * @return wheel cadence in revolutions per minute.
	 */
	public float getWheelCadence(final CyclingSpeedAndCadenceMeasurementResponse previous) {
		float timeDifference;
		if (lastWheelEventTime < previous.lastWheelEventTime)
			timeDifference = (65535 + lastWheelEventTime - previous.lastWheelEventTime) / 1024.0f; // [s]
		else
			timeDifference = (lastWheelEventTime - previous.lastWheelEventTime) / 1024.0f; // [s]

		if (timeDifference == 0)
			return 0.0f;

		return (wheelRevolutions - previous.wheelRevolutions) * 60.0f / timeDifference; // [revolutions/minute];
	}

	/**
	 * Returns average crank cadence since the previous message was received.
	 *
	 * @param previous a previous response.
	 * @return crank cadence in revolutions per minute.
	 */
	public float getCrankCadence(final CyclingSpeedAndCadenceMeasurementResponse previous) {
		float timeDifference;
		if (lastCrankEventTime < previous.lastCrankEventTime)
			timeDifference = (65535 + lastCrankEventTime - previous.lastCrankEventTime) / 1024.0f; // [s]
		else
			timeDifference = (lastCrankEventTime - previous.lastCrankEventTime) / 1024.0f; // [s]

		if (timeDifference == 0)
			return 0.0f;

		return (crankRevolutions - previous.crankRevolutions) * 60.0f / timeDifference; // [revolutions/minute];
	}

	/**
	 * Returns the gear ratio (equal to wheel cadence / crank cadence).
	 * @param previous a previous response.
	 * @return gear ratio.
	 */
	public float getGearRatio(final CyclingSpeedAndCadenceMeasurementResponse previous) {
		final float crankCadence = getCrankCadence(previous);
		if (crankCadence > 0) {
			return getWheelCadence(previous) / crankCadence;
		} else {
			return 0.0f;
		}
	}

	// Parcelable
	private CyclingSpeedAndCadenceMeasurementResponse(final Parcel in) {
		super(in);
		wheelRevolutions = in.readLong();
		crankRevolutions = in.readLong();
		lastWheelEventTime = in.readInt();
		lastCrankEventTime = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(wheelRevolutions);
		dest.writeLong(crankRevolutions);
		dest.writeInt(lastWheelEventTime);
		dest.writeInt(lastCrankEventTime);
	}

	public static final Creator<CyclingSpeedAndCadenceMeasurementResponse> CREATOR = new Creator<CyclingSpeedAndCadenceMeasurementResponse>() {
		@Override
		public CyclingSpeedAndCadenceMeasurementResponse createFromParcel(final Parcel in) {
			return new CyclingSpeedAndCadenceMeasurementResponse(in);
		}

		@Override
		public CyclingSpeedAndCadenceMeasurementResponse[] newArray(final int size) {
			return new CyclingSpeedAndCadenceMeasurementResponse[size];
		}
	};
}
