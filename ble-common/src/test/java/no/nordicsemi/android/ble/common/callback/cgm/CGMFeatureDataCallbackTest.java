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

package no.nordicsemi.android.ble.common.callback.cgm;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class CGMFeatureDataCallbackTest {
	private boolean called;

	@Test
	public void onContinuousGlucoseMeasurementFeaturesReceived_full() {
		final DataReceivedCallback callback = new CGMFeatureDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorFeaturesReceived(@NonNull final BluetoothDevice device, @NonNull final CGMFeatures features,
																   final int type, final int sampleLocation, final boolean secured) {
				called = true;
				assertNotNull(features);
				assertFalse(features.calibrationSupported);
				assertTrue(features.patientHighLowAlertsSupported);
				assertTrue(features.hypoAlertsSupported);
				assertTrue(features.hyperAlertsSupported);
				assertFalse(features.rateOfIncreaseDecreaseAlertsSupported);
				assertTrue(features.deviceSpecificAlertSupported);
				assertTrue(features.sensorMalfunctionDetectionSupported);
				assertFalse(features.sensorTempHighLowDetectionSupported);
				assertFalse(features.sensorResultHighLowSupported);
				assertTrue(features.lowBatteryDetectionSupported);
				assertTrue(features.sensorTypeErrorDetectionSupported);
				assertTrue(features.generalDeviceFaultSupported);
				assertTrue(features.e2eCrcSupported);
				assertFalse(features.multipleBondSupported);
				assertFalse(features.multipleSessionsSupported);
				assertTrue(features.cgmTrendInfoSupported);
				assertTrue(features.cgmQualityInfoSupported);
				assertEquals("Type", TYPE_ARTERIAL_PLASMA, type);
				assertEquals("Sample Location", SAMPLE_LOCATION_FINGER, sampleLocation);
				assertTrue(secured);
			}

			@Override
			public void onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid CRC reported", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid data reported", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[6]);
		assertTrue(data.setValue(0b11001111001101110, Data.FORMAT_UINT24, 0));
		assertTrue(data.setValue(0x16, Data.FORMAT_UINT8, 3));
		assertTrue(data.setValue(0xC18A, Data.FORMAT_UINT16, 4));
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onContinuousGlucoseMeasurementFeaturesReceived_crcNotSupported() {
		final DataReceivedCallback callback = new CGMFeatureDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorFeaturesReceived(@NonNull final BluetoothDevice device, @NonNull final CGMFeatures features,
																   final int type, final int sampleLocation, final boolean secured) {
				called = true;
				assertNotNull(features);
				assertFalse(features.calibrationSupported);
				assertTrue(features.patientHighLowAlertsSupported);
				assertTrue(features.hypoAlertsSupported);
				assertTrue(features.hyperAlertsSupported);
				assertFalse(features.rateOfIncreaseDecreaseAlertsSupported);
				assertTrue(features.deviceSpecificAlertSupported);
				assertTrue(features.sensorMalfunctionDetectionSupported);
				assertFalse(features.sensorTempHighLowDetectionSupported);
				assertFalse(features.sensorResultHighLowSupported);
				assertTrue(features.lowBatteryDetectionSupported);
				assertTrue(features.sensorTypeErrorDetectionSupported);
				assertTrue(features.generalDeviceFaultSupported);
				assertFalse(features.e2eCrcSupported);
				assertFalse(features.multipleBondSupported);
				assertFalse(features.multipleSessionsSupported);
				assertTrue(features.cgmTrendInfoSupported);
				assertTrue(features.cgmQualityInfoSupported);
				assertEquals("Type", TYPE_ARTERIAL_PLASMA, type);
				assertEquals("Sample Location", SAMPLE_LOCATION_FINGER, sampleLocation);
				assertFalse(secured);
			}

			@Override
			public void onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid CRC reported", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid data reported", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[6]);
		data.setValue(0b11000111001101110, Data.FORMAT_UINT24, 0);
		data.setValue(0x16, Data.FORMAT_UINT8, 3);
		data.setValue(0xFFFF, Data.FORMAT_UINT16, 4);
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onContinuousGlucoseMeasurementFeaturesReceivedWithCrcError() {
		final DataReceivedCallback callback = new CGMFeatureDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorFeaturesReceived(@NonNull final BluetoothDevice device, @NonNull final CGMFeatures features,
																   final int type, final int sampleLocation, final boolean secured) {
				assertEquals("Wrong CRC but data reported", 1, 2);
			}

			@Override
			public void onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				called = true;
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Wrong CRC but invalid data reported", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[6]);
		assertTrue(data.setValue(0b11001111001101110, Data.FORMAT_UINT24, 0));
		assertTrue(data.setValue(0x16, Data.FORMAT_UINT8, 3));
		assertTrue(data.setValue(0xBEAF, Data.FORMAT_UINT16, 4));
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onInvalidDataReceived_noCrc() {
		final DataReceivedCallback callback = new CGMFeatureDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorFeaturesReceived(@NonNull final BluetoothDevice device, @NonNull final CGMFeatures features,
																   final int type, final int sampleLocation, final boolean secured) {
				assertEquals("Invalid data but data reported", 1, 2);
			}

			@Override
			public void onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid data but wrong CRC reported", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				called = true;
			}
		};
		final MutableData data = new MutableData(new byte[4]);
		assertTrue(data.setValue(0b11001111001101110, Data.FORMAT_UINT24, 0));
		assertTrue(data.setValue(0x16, Data.FORMAT_UINT8, 3));
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onInvalidDataReceived_wrongDefaultCrc() {
		final DataReceivedCallback callback = new CGMFeatureDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorFeaturesReceived(@NonNull final BluetoothDevice device, @NonNull final CGMFeatures features,
																   final int type, final int sampleLocation, final boolean secured) {
				assertEquals("Wrong CRC but data reported", 1, 2);
			}

			@Override
			public void onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid CRC reported", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				called = true;
			}
		};
		final MutableData data = new MutableData(new byte[6]);
		assertTrue(data.setValue(0b11000111001101110, Data.FORMAT_UINT24, 0));
		assertTrue(data.setValue(0x16, Data.FORMAT_UINT8, 3));
		assertTrue(data.setValue(0xBEAF, Data.FORMAT_UINT16, 4));
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}
}