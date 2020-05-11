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
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public interface CyclingSpeedAndCadenceMeasurementCallback {

	/**
	 * Method called when the data received had wheel revolution data present.
	 * The default implementation calculates the total distance, distance since connection and
	 * current speed and calls
	 * {@link CyclingSpeedAndCadenceCallback#onDistanceChanged(BluetoothDevice, float, float, float)}.
	 *
	 * @param device             the target device.
	 * @param wheelRevolutions   cumulative wheel revolutions since the CSC device was reset (UINT32).
	 * @param lastWheelEventTime the last wheel event time in 1/1024 s (UINT16).
	 */
	void onWheelMeasurementReceived(@NonNull final BluetoothDevice device,
									@IntRange(from = 0, to = 4294967295L) final long wheelRevolutions,
									@IntRange(from = 0, to = 65535) final int lastWheelEventTime);

	/**
	 * Method called when the data received had crank revolution data present.
	 * The default implementation calculates the crank cadence and gear ratio and
	 * calls {@link CyclingSpeedAndCadenceCallback#onCrankDataChanged(BluetoothDevice, float, float)}
	 *
	 * @param device             the target device.
	 * @param crankRevolutions   cumulative crank revolutions since the CSC device was reset (UINT16).
	 * @param lastCrankEventTime the last crank event time in 1/1024 s (UINT16).
	 */
	void onCrankMeasurementReceived(@NonNull final BluetoothDevice device,
									@IntRange(from = 0, to = 65535) final int crankRevolutions,
									@IntRange(from = 0, to = 65535) final int lastCrankEventTime);
}
