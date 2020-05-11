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

package no.nordicsemi.android.ble.common.profile.glucose;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public interface GlucoseFeatureCallback {

	@SuppressWarnings("WeakerAccess")
	class GlucoseFeatures {
		public final boolean lowBatteryDetectionSupported;
		public final boolean sensorMalfunctionDetectionSupported;
		public final boolean sensorSampleSizeSupported;
		public final boolean sensorStripInsertionErrorDetectionSupported;
		public final boolean sensorStripTypeErrorDetectionSupported;
		public final boolean sensorResultHighLowSupported;
		public final boolean sensorTempHighLowDetectionSupported;
		public final boolean sensorReadInterruptDetectionSupported;
		public final boolean generalDeviceFaultSupported;
		public final boolean timeFaultSupported;
		public final boolean multipleBondSupported;
		public final int value;

		public GlucoseFeatures(final int features) {
			this.value = features;

			lowBatteryDetectionSupported = (features & 0x0001) != 0;
			sensorMalfunctionDetectionSupported = (features & 0x0002) != 0;
			sensorSampleSizeSupported = (features & 0x0004) != 0;
			sensorStripInsertionErrorDetectionSupported = (features & 0x0008) != 0;
			sensorStripTypeErrorDetectionSupported = (features & 0x0010) != 0;
			sensorResultHighLowSupported = (features & 0x0020) != 0;
			sensorTempHighLowDetectionSupported = (features & 0x0040) != 0;
			sensorReadInterruptDetectionSupported = (features & 0x0080) != 0;
			generalDeviceFaultSupported = (features & 0x0100) != 0;
			timeFaultSupported = (features & 0x0200) != 0;
			multipleBondSupported = (features & 0x0400) != 0;
		}
	}

	/**
	 * Callback called when Glucose Feature value was received.
	 *
	 * @param device   the target device.
	 * @param features the features supported by the target device.
	 */
	void onGlucoseFeaturesReceived(@NonNull final BluetoothDevice device,
								   @NonNull final GlucoseFeatures features);
}
