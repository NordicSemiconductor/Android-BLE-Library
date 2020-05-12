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
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.common.profile.glucose.GlucoseSampleType;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseSampleLocation;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("unused")
public interface CGMFeatureCallback extends CGMTypes {

	/**
	 * Callback called when Continuous Glucose Monitor Feature value was received.
	 * <p>
	 * If the E2E CRC field was present in the CGM packet, the data has been verified against it.
	 * If CRC check has failed, the
	 * {@link #onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(BluetoothDevice, Data)}
	 * will be called instead.
	 *
	 * @param device         the target device.
	 * @param features       the features supported by the target device.
	 * @param type           the sample type, see TYPE_* constants.
	 * @param sampleLocation the sample location, see SAMPLE_LOCATION_* constants.
	 * @param secured        true, if the value received was secured with E2E-CRC value and the
	 *                       CRC matched the packet. False, if the
	 *                       {@link CGMFeatures#e2eCrcSupported} feature is not supported.
	 */
	void onContinuousGlucoseMonitorFeaturesReceived(
			@NonNull final BluetoothDevice device,
			@NonNull final CGMFeatures features,
			@GlucoseSampleType final int type,
			@GlucoseSampleLocation final int sampleLocation,
			final boolean secured);

	/**
	 * Callback called when a CGM Feature packet with E2E field was received but the CRC check
	 * has failed.
	 *
	 * @param device the target device.
	 * @param data   the CGM Feature packet data that was received, including the CRC field.
	 */
	default void onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(
			@NonNull final BluetoothDevice device,
			@NonNull final Data data) {
		// ignore
	}
}
