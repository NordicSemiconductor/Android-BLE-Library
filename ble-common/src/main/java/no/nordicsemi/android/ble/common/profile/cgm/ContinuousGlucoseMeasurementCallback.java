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

package no.nordicsemi.android.ble.common.profile.cgm;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("unused")
public interface ContinuousGlucoseMeasurementCallback extends CGMTypes {

	/**
	 * Callback called when a Continuous Glucose Measurement packet has been received.
	 * <p>
	 * If the E2E CRC field was present in the CGM packet, the data has been verified against it.
	 * If CRC check has failed, the {@link #onContinuousGlucoseMeasurementReceivedWithCrcError(BluetoothDevice, Data)}
	 * will be called instead.
	 * <p>
	 * The Glucose concentration is reported in mg/dL. To convert it to mmol/L use:
	 * <pre>value [mg/dL] = 18.02 * value [mmol/L]</pre>
	 * or simply call {@link #toMgPerDecilitre(float)}.
	 * <p>
	 * Note that the conversion factor is compliant to the Continua blood glucose meter specification.
	 *
	 * @param device               the target device.
	 * @param glucoseConcentration the glucose concentration in mg/dL.
	 * @param cgmTrend             an optional CGM Trend information, in (mg/dL)/min.
	 * @param cgmQuality           an optional CGM Quality information in percent.
	 * @param status               the status of the measurement.
	 * @param timeOffset           the time offset in minutes since Session Start Time.
	 * @param secured              true if the packet was sent with E2E-CRC value that was verified
	 *                             to match the packet, false if the packet didn't contain CRC field.
	 *                             For a callback in case of invalid CRC value check
	 *                             {@link #onContinuousGlucoseMeasurementReceivedWithCrcError(BluetoothDevice, Data)}.
	 */
	void onContinuousGlucoseMeasurementReceived(@NonNull final BluetoothDevice device,
												@FloatRange(from = 0) final float glucoseConcentration,
												@Nullable final Float cgmTrend,
												@Nullable final Float cgmQuality,
												@Nullable final CGMStatus status,
												@IntRange(from = 0) final int timeOffset,
												final boolean secured);

	/**
	 * Callback called when a CGM packet with E2E field was received but the CRC check has failed.
	 *
	 * @param device the target device.
	 * @param data   the CGM packet data that was received, including the CRC field.
	 */
	default void onContinuousGlucoseMeasurementReceivedWithCrcError(
			@NonNull final BluetoothDevice device,
			@NonNull final Data data) {
		// ignore
	}

	/**
	 * Converts the value from mmol/L to mg/dL.
	 *
	 * @param value the glucose concentration value in given unit.
	 * @return Value in mg/dL.
	 */
	static float toMgPerDecilitre(final float value) {
		return value * 18.2f;
	}
}
