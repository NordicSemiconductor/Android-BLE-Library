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

package no.nordicsemi.android.ble.common.profile.rsc;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public interface RunningSpeedAndCadenceFeatureCallback {

	@SuppressWarnings("WeakerAccess")
	class RSCFeatures {
		public final boolean instantaneousStrideLengthMeasurementSupported;
		public final boolean totalDistanceMeasurementSupported;
		public final boolean walkingOrRunningStatusSupported;
		public final boolean calibrationProcedureSupported;
		public final boolean multipleSensorLocationsSupported;
		public final int value;

		public RSCFeatures(final int features) {
			this.value = features;

			instantaneousStrideLengthMeasurementSupported = (features & 0x0001) != 0;
			totalDistanceMeasurementSupported = (features & 0x0002) != 0;
			walkingOrRunningStatusSupported = (features & 0x0004) != 0;
			calibrationProcedureSupported = (features & 0x0008) != 0;
			multipleSensorLocationsSupported = (features & 0x0010) != 0;
		}
	}

	/**
	 * Method called when the RSC Feature characteristic has been read.
	 *
	 * @param device   the target device.
	 * @param features the device features.
	 */
	void onRunningSpeedAndCadenceFeaturesReceived(@NonNull final BluetoothDevice device,
												  @NonNull final RSCFeatures features);
}
