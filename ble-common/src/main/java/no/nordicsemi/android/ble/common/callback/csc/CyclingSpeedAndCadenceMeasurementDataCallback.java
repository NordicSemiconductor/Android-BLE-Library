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
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.csc.CyclingSpeedAndCadenceCallback;
import no.nordicsemi.android.ble.common.profile.csc.CyclingSpeedAndCadenceMeasurementCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses value into CSC Measurement data.
 * If the value received do not match required syntax
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.csc_measurement.xml
 */
@SuppressWarnings({"WeakerAccess", "unused", "ConstantConditions"})
public abstract class CyclingSpeedAndCadenceMeasurementDataCallback extends ProfileReadResponse
		implements CyclingSpeedAndCadenceMeasurementCallback, CyclingSpeedAndCadenceCallback {
	private long mInitialWheelRevolutions = -1;
	private long mLastWheelRevolutions = -1;
	private int mLastWheelEventTime = -1;
	private int mLastCrankRevolutions = -1;
	private int mLastCrankEventTime = -1;
	private float mWheelCadence = -1;

	public CyclingSpeedAndCadenceMeasurementDataCallback() {
		// empty
	}

	protected CyclingSpeedAndCadenceMeasurementDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() < 1) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Decode the new data
		int offset = 0;
		final int flags = data.getByte(offset);
		offset += 1;

		final boolean wheelRevPresent = (flags & 0x01) != 0;
		final boolean crankRevPreset = (flags & 0x02) != 0;

		if (data.size() < 1 + (wheelRevPresent ? 6 : 0) + (crankRevPreset ? 4 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		if (wheelRevPresent) {
			final long wheelRevolutions = data.getIntValue(Data.FORMAT_UINT32, offset) & 0xFFFFFFFFL;
			offset += 4;

			final int lastWheelEventTime = data.getIntValue(Data.FORMAT_UINT16, offset); // 1/1024 s
			offset += 2;

			if (mInitialWheelRevolutions < 0)
				mInitialWheelRevolutions = wheelRevolutions;

			// Notify listener about the new measurement
			onWheelMeasurementReceived(device, wheelRevolutions, lastWheelEventTime);
		}

		if (crankRevPreset) {
			final int crankRevolutions = data.getIntValue(Data.FORMAT_UINT16, offset);
			offset += 2;

			final int lastCrankEventTime = data.getIntValue(Data.FORMAT_UINT16, offset);
			// offset += 2;

			// Notify listener about the new measurement
			onCrankMeasurementReceived(device, crankRevolutions, lastCrankEventTime);
		}
	}

	@Override
	public void onWheelMeasurementReceived(@NonNull final BluetoothDevice device, final long wheelRevolutions, final int lastWheelEventTime) {
		if (mLastWheelEventTime == lastWheelEventTime)
			return;

		if (mLastWheelRevolutions >= 0) {
			final float circumference = getWheelCircumference();

			float timeDifference;
			if (lastWheelEventTime < mLastWheelEventTime)
				timeDifference = (65535 + lastWheelEventTime - mLastWheelEventTime) / 1024.0f; // [s]
			else
				timeDifference = (lastWheelEventTime - mLastWheelEventTime) / 1024.0f; // [s]
			final float distanceDifference = (wheelRevolutions - mLastWheelRevolutions) * circumference / 1000.0f; // [m]
			final float totalDistance = (float) wheelRevolutions * circumference / 1000.0f; // [m]
			final float distance = (float) (wheelRevolutions - mInitialWheelRevolutions) * circumference / 1000.0f; // [m]
			final float speed = distanceDifference / timeDifference; // [m/s]
			mWheelCadence = (wheelRevolutions - mLastWheelRevolutions) * 60.0f / timeDifference; // [revolutions/minute]

			// Notify listener about the new measurement
			onDistanceChanged(device, totalDistance, distance, speed);
		}
		mLastWheelRevolutions = wheelRevolutions;
		mLastWheelEventTime = lastWheelEventTime;
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public void onCrankMeasurementReceived(@NonNull final BluetoothDevice device, final int crankRevolutions, final int lastCrankEventTime) {
		if (mLastCrankEventTime == lastCrankEventTime)
			return;

		if (mLastCrankRevolutions >= 0) {
			float timeDifference;
			if (lastCrankEventTime < mLastCrankEventTime)
				timeDifference = (65535 + lastCrankEventTime - mLastCrankEventTime) / 1024.0f; // [s]
			else
				timeDifference = (lastCrankEventTime - mLastCrankEventTime) / 1024.0f; // [s]

			final float crankCadence = (crankRevolutions - mLastCrankRevolutions) * 60.0f / timeDifference; // [revolutions/minute]
			if (crankCadence > 0) {
				final float gearRatio = mWheelCadence >= 0 ? mWheelCadence / crankCadence : 0.0f;

				// Notify listener about the new measurement
				onCrankDataChanged(device, crankCadence, gearRatio);
			} else {
				// Notify listener about the new measurement
				// onCrankDataChanged(device, 0, 0);
			}
		}
		mLastCrankRevolutions = crankRevolutions;
		mLastCrankEventTime = lastCrankEventTime;
	}
}
