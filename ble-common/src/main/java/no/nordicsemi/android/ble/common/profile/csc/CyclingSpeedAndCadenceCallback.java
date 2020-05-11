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

package no.nordicsemi.android.ble.common.profile.csc;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

public interface CyclingSpeedAndCadenceCallback {
	float WHEEL_CIRCUMFERENCE_DEFAULT = 2340; // [mm]

	/**
	 * This method should return the wheel circumference in millimeters.
	 * See http://www.bikecalc.com/wheel_size_math for values.
	 *
	 * @return wheel circumference in mm. By default it returns {@link #WHEEL_CIRCUMFERENCE_DEFAULT}.
	 */
	@SuppressWarnings("SameReturnValue")
	default float getWheelCircumference() {
		return WHEEL_CIRCUMFERENCE_DEFAULT;
	}

	/**
	 * Callback called when the traveled distance and speed has changed.
	 * The distance and speed calculations are based on the wheel circumference obtained
	 * with {@link #getWheelCircumference()}. Make sure it returns the correct value.
	 *
	 * @param device        the target device.
	 * @param totalDistance the total distance traveled since the measuring device was reset, in meters.
	 * @param distance      the distance traveled since the phone connected to the CSC device, in meters.
	 * @param speed         current speed, in meters per second.
	 */
	void onDistanceChanged(@NonNull final BluetoothDevice device,
						   @FloatRange(from = 0) final float totalDistance,
						   @FloatRange(from = 0) final float distance,
						   @FloatRange(from = 0) final float speed);

	/**
	 * Callback called when the crank data (cadence or gear ratio) has changed.
	 *
	 * @param device       the target device.
	 * @param crankCadence new crank cadence, in revolutions per minute.
	 * @param gearRatio    new gear ratio.
	 */
	void onCrankDataChanged(@NonNull final BluetoothDevice device,
							@FloatRange(from = 0) final float crankCadence,
							final float gearRatio);
}
