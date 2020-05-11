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

package no.nordicsemi.android.ble.common.callback.glucose;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseFeatureCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class GlucoseFeatureDataCallbackTest {
	private boolean success;
	private boolean invalidData;
	private GlucoseFeatureCallback.GlucoseFeatures result;

	private final DataReceivedCallback callback = new GlucoseFeatureDataCallback() {
		@Override
		public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			success = false;
			invalidData = false;
			result = null;
			super.onDataReceived(device, data);
		}

		@Override
		public void onGlucoseFeaturesReceived(@NonNull final BluetoothDevice device, @NonNull final GlucoseFeatures features) {
			success = true;
			result = features;
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			invalidData = true;
		}
	};

	@Test
	public void onGlucoseFeaturesReceived() {
		final Data data = new Data(new byte[] { (byte) 0b11110000, (byte) 0b00000011 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertFalse(result.lowBatteryDetectionSupported);
		assertFalse(result.sensorMalfunctionDetectionSupported);
		assertFalse(result.sensorSampleSizeSupported);
		assertFalse(result.sensorStripInsertionErrorDetectionSupported);
		assertTrue(result.sensorStripTypeErrorDetectionSupported);
		assertTrue(result.sensorResultHighLowSupported);
		assertTrue(result.sensorTempHighLowDetectionSupported);
		assertTrue(result.sensorReadInterruptDetectionSupported);
		assertTrue(result.generalDeviceFaultSupported);
		assertTrue(result.timeFaultSupported);
		assertFalse(result.multipleBondSupported);
	}

	@Test
	public void onInvalidDataReceived() {
		final Data data = new Data();
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}
}