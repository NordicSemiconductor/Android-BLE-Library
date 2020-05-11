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
public class CGMStatusDataCallbackTest {
	private boolean called = false;

	@Test
	public void onContinuousGlucoseMonitorStatusChanged_withCrc() {
		final DataReceivedCallback callback = new CGMStatusDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorStatusChanged(@NonNull final BluetoothDevice device, @NonNull final CGMStatus status,
																final int timeOffset, final boolean secured) {
				assertNotNull("Status present", status);
				assertTrue(status.sessionStopped);
				assertTrue(status.deviceBatteryLow);
				assertTrue(status.sensorTypeIncorrectForDevice);
				assertTrue(status.sensorMalfunction);
				assertTrue(status.deviceSpecificAlert);
				assertTrue(status.generalDeviceFault);
				assertTrue(status.timeSyncRequired);
				assertTrue(status.calibrationNotAllowed);
				assertTrue(status.calibrationRecommended);
				assertTrue(status.calibrationRequired);
				assertTrue(status.sensorTemperatureTooHigh);
				assertTrue(status.sensorTemperatureTooLow);
				assertTrue(status.sensorResultLowerThenPatientLowLevel);
				assertTrue(status.sensorResultHigherThenPatientHighLevel);
				assertTrue(status.sensorResultLowerThenHypoLevel);
				assertTrue(status.sensorResultHigherThenHyperLevel);
				assertTrue(status.sensorRateOfDecreaseExceeded);
				assertTrue(status.sensorRateOfIncreaseExceeded);
				assertTrue(status.sensorResultLowerThenDeviceCanProcess);
				assertTrue(status.sensorResultHigherThenDeviceCanProcess);
				assertEquals("Time offset", 5, timeOffset);
				assertTrue(secured);
			}

			@Override
			public void onContinuousGlucoseMonitorStatusReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as CRC error", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[7]);
		data.setValue(5, Data.FORMAT_UINT16, 0);
		data.setValue(0xff3f3f, Data.FORMAT_UINT24, 2); // all flags set
		data.setValue(0xE0A7, Data.FORMAT_UINT16, 5);
		callback.onDataReceived(null, data);
	}

	@Test
	public void onContinuousGlucoseMonitorStatusChanged_noCrc() {
		final DataReceivedCallback callback = new CGMStatusDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorStatusChanged(@NonNull final BluetoothDevice device, @NonNull final CGMStatus status,
																final int timeOffset, final boolean secured) {
				assertNotNull("Status present", status);
				assertTrue(status.sessionStopped);
				assertFalse(status.deviceBatteryLow);
				assertFalse(status.sensorTypeIncorrectForDevice);
				assertFalse(status.sensorMalfunction);
				assertFalse(status.deviceSpecificAlert);
				assertFalse(status.generalDeviceFault);
				assertTrue(status.timeSyncRequired);
				assertFalse(status.calibrationNotAllowed);
				assertFalse(status.calibrationRecommended);
				assertFalse(status.calibrationRequired);
				assertFalse(status.sensorTemperatureTooHigh);
				assertFalse(status.sensorTemperatureTooLow);
				assertTrue(status.sensorResultLowerThenPatientLowLevel);
				assertFalse(status.sensorResultHigherThenPatientHighLevel);
				assertFalse(status.sensorResultLowerThenHypoLevel);
				assertFalse(status.sensorResultHigherThenHyperLevel);
				assertFalse(status.sensorRateOfDecreaseExceeded);
				assertFalse(status.sensorRateOfIncreaseExceeded);
				assertFalse(status.sensorResultLowerThenDeviceCanProcess);
				assertFalse(status.sensorResultHigherThenDeviceCanProcess);
				assertEquals("Time offset", 6, timeOffset);
				assertFalse(secured);
			}

			@Override
			public void onContinuousGlucoseMonitorStatusReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as CRC error", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[5]);
		data.setValue(6, Data.FORMAT_UINT16, 0);
		data.setValue(0x010101, Data.FORMAT_UINT24, 2);
		callback.onDataReceived(null, data);
	}

	@Test
	public void onContinuousGlucoseMonitorStatusReceivedWithCrcError() {
		final DataReceivedCallback callback = new CGMStatusDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorStatusChanged(@NonNull final BluetoothDevice device, @NonNull final CGMStatus status,
																final int timeOffset, final boolean secured) {
				assertEquals("Invalid CRC reported as valid packet", 1, 2);
			}

			@Override
			public void onContinuousGlucoseMonitorStatusReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				called = true;
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[7]);
		data.setValue(6, Data.FORMAT_UINT16, 0);
		data.setValue(0x010101, Data.FORMAT_UINT24, 2);
		data.setValue(0xE0A7, Data.FORMAT_UINT16, 5);
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onInvalidDataReceived() {
		final DataReceivedCallback callback = new CGMStatusDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorStatusChanged(@NonNull final BluetoothDevice device, @NonNull final CGMStatus status,
																final int timeOffset, final boolean secured) {
				assertEquals("Invalid data reported as valid packet", 1, 2);
			}

			@Override
			public void onContinuousGlucoseMonitorStatusReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid data reported as wrong CRC", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				called = true;
			}
		};
		final MutableData data = new MutableData(new byte[6]);
		data.setValue(6, Data.FORMAT_UINT16, 0);
		data.setValue(0x010101, Data.FORMAT_UINT24, 2);
		data.setValue(1, Data.FORMAT_UINT8, 5);
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}
}