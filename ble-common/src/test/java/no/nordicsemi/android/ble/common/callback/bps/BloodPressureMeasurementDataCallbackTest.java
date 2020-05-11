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

package no.nordicsemi.android.ble.common.callback.bps;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class BloodPressureMeasurementDataCallbackTest {

	@Test
	public void onBloodPressureMeasurementReceived_full() {
		final DataReceivedCallback callback = new BloodPressureMeasurementDataCallback() {
			@Override
			public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
														   final float systolic, final float diastolic, final float meanArterialPressure, final int unit,
														   @Nullable final Float pulseRate, @Nullable final Integer userID,
														   @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
				assertEquals("Systolic", 138.0, systolic, 0);
				assertEquals("Diastolic", 88.0, diastolic, 0);
				assertEquals("Mean AP", 120.0, meanArterialPressure, 0);
				assertEquals("Unit: mmHg", 0, unit);
				assertNotNull("Pulse rate set", pulseRate);
				assertEquals("Pulse rate", 60.0, pulseRate, 0);
				assertNotNull("User ID set", userID);
				assertEquals("User ID", 1, userID.intValue());
				assertNotNull("Status set", status);
				assertTrue(status.bodyMovementDetected);
				assertTrue(status.cuffTooLose);
				assertTrue(status.irregularPulseDetected);
				assertTrue(status.pulseRateInRange);
				assertFalse(status.pulseRateExceedsUpperLimit);
				assertFalse(status.pulseRateIsLessThenLowerLimit);
				assertTrue(status.improperMeasurementPosition);
				assertNotNull("Calendar set", calendar);
				assertFalse(calendar.isSet(Calendar.YEAR));
				assertTrue(calendar.isSet(Calendar.MONTH));
				assertTrue(calendar.isSet(Calendar.DATE));
				assertEquals("Month", Calendar.APRIL, calendar.get(Calendar.MONTH));
				assertEquals("Day", 17, calendar.get(Calendar.DATE));
				assertEquals("Hour", 20, calendar.get(Calendar.HOUR_OF_DAY));
				assertEquals("Minute", 41, calendar.get(Calendar.MINUTE));
				assertEquals("Second", 59, calendar.get(Calendar.SECOND));
				assertEquals("Milliseconds", 0, calendar.get(Calendar.MILLISECOND));
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct BPM reported as invalid", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[19]);
		// Flags
		data.setByte((byte) 0b11110, 0);
		// Systolic, diastolic and mean AP in mmHg
		data.setValue(138, 0, Data.FORMAT_SFLOAT, 1);
		data.setValue(88, 0, Data.FORMAT_SFLOAT, 3);
		data.setValue(120, 0, Data.FORMAT_SFLOAT, 5);
		// Date and Time
		data.setValue(0, Data.FORMAT_UINT16, 7);
		data.setValue(4, Data.FORMAT_UINT8, 9);
		data.setValue(17, Data.FORMAT_UINT8, 10);
		data.setValue(20, Data.FORMAT_UINT8, 11);
		data.setValue(41, Data.FORMAT_UINT8, 12);
		data.setValue(59, Data.FORMAT_UINT8, 13);
		// Pulse rate
		data.setValue(60, 0, Data.FORMAT_SFLOAT, 14);
		// User ID
		data.setValue(1, Data.FORMAT_UINT8, 16);
		// Measurement status
		data.setValue(0b100111, Data.FORMAT_UINT16, 17);

		assertArrayEquals(
				new byte[] { 0x1E, (byte) 0x8A, 0x00, 0x58, 0x00, 0x78, 0x00, 0x00, 0x00, 0x04, 0x11, 0x14, 0x29, 0x3B, 0x3C, 0x00, 0x01, 0x27, 0x00 },
				data.getValue()
		);

		callback.onDataReceived(null, data);
	}

	@Test
	public void onBloodPressureMeasurementReceived_some() {
		final DataReceivedCallback callback = new BloodPressureMeasurementDataCallback() {
			@Override
			public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
														   final float systolic, final float diastolic, final float meanArterialPressure, final int unit,
														   @Nullable final Float pulseRate, @Nullable final Integer userID,
														   @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
				assertEquals("Systolic", 18.9, systolic, 0.01);
				assertEquals("Diastolic", 11.0, diastolic, 0);
				assertEquals("Mean AP", 15.9, meanArterialPressure, 0.01);
				assertEquals("Unit: kPa", 1, unit);
				assertNotNull("Pulse rate set", pulseRate);
				assertEquals("Pulse rate", 60.0, pulseRate, 0);
				assertNull("User ID not set", userID);
				assertNotNull("Status set", status);
				assertFalse(status.bodyMovementDetected);
				assertTrue(status.cuffTooLose);
				assertFalse(status.irregularPulseDetected);
				assertFalse(status.pulseRateInRange);
				assertFalse(status.pulseRateExceedsUpperLimit);
				assertTrue(status.pulseRateIsLessThenLowerLimit);
				assertFalse(status.improperMeasurementPosition);
				assertNull("Calendar not set", calendar);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct BPM reported as invalid", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[11]);
		// Flags
		data.setByte((byte) 0b10101, 0);
		// Systolic, diastolic and mean AP in mmHg
		data.setValue(189, -1, Data.FORMAT_SFLOAT, 1);
		data.setValue(11, 0, Data.FORMAT_SFLOAT, 3);
		data.setValue(159, -1, Data.FORMAT_SFLOAT, 5);
		// Pulse rate
		data.setValue(60, 0, Data.FORMAT_SFLOAT, 7);
		// Measurement status
		data.setValue(0b010010, Data.FORMAT_UINT16, 9);

		assertArrayEquals(
				new byte[] { 0x15, (byte) 0xBD, (byte) 0xF0, 0xB, 0x0, (byte) 0x9F, (byte) 0xF0, 0x3C, 0x0, 0x12, 0x0 },
				data.getValue()
		);
		callback.onDataReceived(null, data);
	}

	@Test
	public void onBloodPressureMeasurementReceived_minimal() {
		final DataReceivedCallback callback = new BloodPressureMeasurementDataCallback() {
			@Override
			public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
														   final float systolic, final float diastolic, final float meanArterialPressure, final int unit,
														   @Nullable final Float pulseRate, @Nullable final Integer userID,
														   @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
				assertEquals("Systolic", 18.9, systolic, 0.01);
				assertEquals("Diastolic", 11.0, diastolic, 0);
				assertEquals("Mean AP", 15.9, meanArterialPressure, 0.01);
				assertEquals("Unit: mmHg", 0, unit);
				assertNull("Pulse rate not set", pulseRate);
				assertNull("User ID not set", userID);
				assertNull("Status not set", status);
				assertNull("Calendar not set", calendar);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct BPM reported as invalid", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[7]);
		// Flags
		data.setByte((byte) 0b00000, 0);
		// Systolic, diastolic and mean AP in mmHg
		data.setValue(189, -1, Data.FORMAT_SFLOAT, 1);
		data.setValue(11, 0, Data.FORMAT_SFLOAT, 3);
		data.setValue(159, -1, Data.FORMAT_SFLOAT, 5);

		assertArrayEquals(
				new byte[] { 0x00, (byte) 0xBD, (byte) 0xF0, 0xB, 0x0, (byte) 0x9F, (byte) 0xF0 },
				data.getValue()
		);
		callback.onDataReceived(null, data);
	}

	@Test
	public void onInvalidDataReceived_toShort() {
		final DataReceivedCallback callback = new BloodPressureMeasurementDataCallback() {
			@Override
			public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
														   final float systolic, final float diastolic, final float meanArterialPressure, final int unit,
														   @Nullable final Float pulseRate, @Nullable final Integer userID,
														   @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
				assertEquals("Invalid data reported as correct", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid BPM", 6, data.size());
			}
		};
		final Data data = new Data(new byte[] { 1, 2, 3, 4, 5, 6 });

		assertArrayEquals(
				new byte[] { 1, 2, 3, 4, 5, 6 },
				data.getValue()
		);
		callback.onDataReceived(null, data);
	}

	@Test
	public void onInvalidDataReceived_noTimestamp() {
		final DataReceivedCallback callback = new BloodPressureMeasurementDataCallback() {
			@Override
			public void onBloodPressureMeasurementReceived(@NonNull final BluetoothDevice device,
														   final float systolic, final float diastolic, final float meanArterialPressure, final int unit,
														   @Nullable final Float pulseRate, @Nullable final Integer userID,
														   @Nullable final BPMStatus status, @Nullable final Calendar calendar) {
				assertEquals("Invalid data reported as correct", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid BPM", 7, data.size());
			}
		};
		final Data data = new Data(new byte[] { 3, 2, 3, 4, 5, 6, 7 });

		assertArrayEquals(
				new byte[] { 3, 2, 3, 4, 5, 6, 7 },
				data.getValue()
		);
		callback.onDataReceived(null, data);
	}
}