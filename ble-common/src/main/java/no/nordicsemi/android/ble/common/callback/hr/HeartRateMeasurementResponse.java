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

package no.nordicsemi.android.ble.common.callback.hr;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     HeartRateMeasurementResponse response = waitForNotification(characteristic)
 *           .awaitValid(HeartRateMeasurementResponse.class);
 *     int heartRate = response.getHeartRate();
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
public final class HeartRateMeasurementResponse extends HeartRateMeasurementDataCallback implements Parcelable {
	private int heartRate;
	@Nullable
	private Boolean contactDetected;
	@Nullable
	private Integer energyExpanded;
	@Nullable
	private List<Integer> rrIntervals;

	public HeartRateMeasurementResponse() {
		// empty
	}

	@Override
	public void onHeartRateMeasurementReceived(@NonNull final BluetoothDevice device,
											   final int heartRate,
											   @Nullable final Boolean contactDetected,
											   @Nullable final Integer energyExpanded,
											   @Nullable final List<Integer> rrIntervals) {
		this.heartRate = heartRate;
		this.contactDetected = contactDetected;
		this.energyExpanded = energyExpanded;
		this.rrIntervals = rrIntervals;
	}

	public int getHeartRate() {
		return heartRate;
	}

	@Nullable
	public Boolean isSensorContactSupported() {
		return heartRate > 0 ? contactDetected != null : null;
	}

	@Nullable
	public Boolean isSensorContactDetected() {
		return contactDetected;
	}

	@Nullable
	public Integer getEnergyExpanded() {
		return energyExpanded;
	}

	@Nullable
	public List<Integer> getRrIntervals() {
		return rrIntervals;
	}

	// Parcelable
	private HeartRateMeasurementResponse(final Parcel in) {
		super(in);
		heartRate = in.readInt();
		byte tmpContactDetected = in.readByte();
		contactDetected = tmpContactDetected == 0 ? null : tmpContactDetected == 1;
		if (in.readByte() == 0) {
			energyExpanded = null;
		} else {
			energyExpanded = in.readInt();
		}
		final int count = in.readInt();
		if (count == 0) {
			rrIntervals = null;
		} else {
			final ArrayList<Integer> intervals = new ArrayList<>(count);
			in.readList(intervals, Integer.class.getClassLoader());
			rrIntervals = Collections.unmodifiableList(intervals);
		}
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(heartRate);
		dest.writeByte((byte) (contactDetected == null ? 0 : contactDetected ? 1 : 2));
		if (energyExpanded == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(energyExpanded);
		}
		if (rrIntervals == null) {
			dest.writeInt(0);
		} else {
			dest.writeInt(rrIntervals.size());
			dest.writeList(rrIntervals);
		}
	}

	public static final Creator<HeartRateMeasurementResponse> CREATOR = new Creator<HeartRateMeasurementResponse>() {
		@Override
		public HeartRateMeasurementResponse createFromParcel(final Parcel in) {
			return new HeartRateMeasurementResponse(in);
		}

		@Override
		public HeartRateMeasurementResponse[] newArray(final int size) {
			return new HeartRateMeasurementResponse[size];
		}
	};
}
