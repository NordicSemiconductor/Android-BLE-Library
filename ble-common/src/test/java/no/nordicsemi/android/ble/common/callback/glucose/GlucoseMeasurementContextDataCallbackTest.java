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
import androidx.annotation.Nullable;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementContextCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class GlucoseMeasurementContextDataCallbackTest {
	private boolean success;
	private boolean invalidData;
	private int number;

	private final DataReceivedCallback callback = new GlucoseMeasurementContextDataCallback() {
		@Override
		public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			success = false;
			invalidData = false;
			super.onDataReceived(device, data);
		}

		@Override
		public void onGlucoseMeasurementContextReceived(@NonNull final BluetoothDevice device, final int sequenceNumber,
														@Nullable final Carbohydrate carbohydrate, @Nullable final Float carbohydrateAmount,
														@Nullable final Meal meal, @Nullable final Tester tester,
														@Nullable final Health health, @Nullable final Integer exerciseDuration,
														@Nullable final Integer exerciseIntensity, @Nullable final Medication medication,
														@Nullable final Float medicationAmount, @Nullable final Integer medicationUnit,
														@Nullable final Float HbA1c) {
			success = true;
			number = sequenceNumber;
			if (number == 0) {
				assertNotNull(carbohydrate);
				assertNotNull(carbohydrateAmount);
				assertNotNull(meal);
				assertNotNull(tester);
				assertNotNull(health);
				assertNotNull(exerciseDuration);
				assertNotNull(exerciseIntensity);
				assertNotNull(medication);
				assertNotNull(medicationAmount);
				assertNotNull(medicationUnit);
				assertNotNull(HbA1c);
				assertSame(Carbohydrate.DINNER, carbohydrate);
				assertSame(Meal.CASUAL, meal);
				assertSame(Tester.HEALTH_CARE_PROFESSIONAL, tester);
				assertSame(Health.MINOR_HEALTH_ISSUES, health);
				assertSame(Medication.LONG_ACTING_INSULIN, medication);
			} else {
				assertNull(carbohydrate);
				assertNull(carbohydrateAmount);
				assertNull(meal);
				assertNull(tester);
				assertNull(health);
				assertNull(exerciseDuration);
				assertNull(exerciseIntensity);
				assertNull(medication);
				assertNull(medicationAmount);
				assertNull(medicationUnit);
				assertNull(HbA1c);
			}
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			invalidData = true;
		}
	};

	@Test
	public void onGlucoseMeasurementContextReceived_full() {
		final MutableData data = new MutableData(new byte[17]);
		data.setValue(0xFF, Data.FORMAT_UINT8, 0); // Flags
		data.setValue(0, Data.FORMAT_UINT16, 1); // Sequence number
		data.setValue(0xb3, Data.FORMAT_UINT8, 3); // Extended flags - ignored
		data.setValue(GlucoseMeasurementContextCallback.Carbohydrate.DINNER.value, Data.FORMAT_UINT8, 4); // Carbohydrate
		data.setValue(100.0f, Data.FORMAT_SFLOAT, 5); // Carbohydrate Amount
		data.setValue(GlucoseMeasurementContextCallback.Meal.CASUAL.value, Data.FORMAT_UINT8, 7); // Meal
		data.setValue(0x12, Data.FORMAT_UINT8, 8); // Tester and Health (health care practitioner, minor issues)
		data.setValue(60, Data.FORMAT_UINT16, 9); // 1 minute of exercise
		data.setValue(50, Data.FORMAT_UINT8, 11); // 50%
		data.setValue(4, Data.FORMAT_UINT8, 12); // Long acting insulin
		data.setValue(123.45f, Data.FORMAT_SFLOAT, 13); // 123.45 ml
		data.setValue(34.5f, Data.FORMAT_SFLOAT, 15); // HbA1c = 34.5%
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertEquals(0, number);
	}

	@Test
	public void onGlucoseMeasurementContextReceived_empty() {
		final MutableData data = new MutableData(new byte[17]);
		data.setValue(0x00, Data.FORMAT_UINT8, 0); // Flags
		data.setValue(1, Data.FORMAT_UINT16, 1);
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertEquals(1, number);
	}

	@Test
	public void onInvalidDataReceived() {
		final MutableData data = new MutableData(new byte[5]);
		data.setValue(0xFF, Data.FORMAT_UINT8, 0); // Flags
		data.setValue(0, Data.FORMAT_UINT16, 1); // Sequence number
		data.setValue(0xb3, Data.FORMAT_UINT8, 3); // Extended flags - ignored
		data.setValue(3, Data.FORMAT_UINT8, 4); // Carbohydrate: dinner
		data.setValue(100.0f, Data.FORMAT_SFLOAT, 5); // Carbohydrate Amount
		data.setValue(4, Data.FORMAT_UINT8, 7); // Meal: casual
		data.setValue(0x12, Data.FORMAT_UINT8, 8); // Tester and Health (health care practitioner, minor issues)
		data.setValue(60, Data.FORMAT_UINT16, 9); // 1 minute of exercise
		data.setValue(50, Data.FORMAT_UINT8, 11); // 50%
		data.setValue(4, Data.FORMAT_UINT8, 12); // Long acting insulin
		data.setValue(123.45f, Data.FORMAT_SFLOAT, 13); // 123.45 ml
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}
}