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
import android.os.Parcel;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementContextCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses value into Glucose Measurement Context data.
 * If the value received do not match required syntax
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.glucose_measurement_context.xml
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public abstract class GlucoseMeasurementContextDataCallback extends ProfileReadResponse implements GlucoseMeasurementContextCallback {

	public GlucoseMeasurementContextDataCallback() {
		// empty
	}

	protected GlucoseMeasurementContextDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);
		
		if (data.size() < 3) {
			onInvalidDataReceived(device, data);
			return;
		}

		int offset = 0;

		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);
		final boolean carbohydratePresent = (flags & 0x01) != 0;
		final boolean mealPresent = (flags & 0x02) != 0;
		final boolean testerHealthPresent = (flags & 0x04) != 0;
		final boolean exercisePresent = (flags & 0x08) != 0;
		final boolean medicationPresent = (flags & 0x10) != 0;
		final boolean medicationUnitLiter = (flags & 0x20) != 0;
		final boolean HbA1cPresent = (flags & 0x40) != 0;
		final boolean extendedFlagsPresent = (flags & 0x80) != 0;

		if (data.size() < 3 + (carbohydratePresent ? 3 : 0) + (mealPresent ? 1 : 0) + (testerHealthPresent ? 1 : 0)
				+ (exercisePresent ? 3 : 0) + (medicationPresent ? 3 : 0) + (HbA1cPresent ? 2 : 0)
				+ (extendedFlagsPresent ? 1 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		final int sequenceNumber = data.getIntValue(Data.FORMAT_UINT16, offset);
		offset += 2;

		// Optional fields
		if (extendedFlagsPresent) {
			// ignore extended flags
			offset += 1;
		}

		Carbohydrate carbohydrate = null;
		Float carbohydrateAmount = null;
		if (carbohydratePresent) {
			final int carbohydrateId = data.getIntValue(Data.FORMAT_UINT8, offset);
			carbohydrate = Carbohydrate.from(carbohydrateId);
			carbohydrateAmount = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 1); // in grams
			offset += 3;
		}

		Meal meal = null;
		if (mealPresent) {
			final int mealId = data.getIntValue(Data.FORMAT_UINT8, offset);
			meal = Meal.from(mealId);
			offset += 1;
		}

		Tester tester = null;
		Health health = null;
		if (testerHealthPresent) {
			final int testerAndHealth = data.getIntValue(Data.FORMAT_UINT8, offset);
			tester = Tester.from(testerAndHealth & 0x0F);
			health = Health.from(testerAndHealth >> 4);
			offset += 1;
		}

		Integer exerciseDuration = null;
		Integer exerciseIntensity = null;
		if (exercisePresent) {
			exerciseDuration = data.getIntValue(Data.FORMAT_UINT16, offset); // in seconds
			exerciseIntensity = data.getIntValue(Data.FORMAT_UINT8, offset + 2); // in percentage
			offset += 3;
		}

		Medication medication = null;
		Float medicationAmount = null;
		Integer medicationUnit = null;
		if (medicationPresent) {
			final int medicationId = data.getIntValue(Data.FORMAT_UINT8, offset);
			medication = Medication.from(medicationId);
			medicationAmount = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 1); // mg or ml
			medicationUnit = medicationUnitLiter ? UNIT_ml : UNIT_mg;
			offset += 3;
		}

		Float HbA1c = null;
		if (HbA1cPresent) {
			HbA1c = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			// offset += 2;
		}

		onGlucoseMeasurementContextReceived(device, sequenceNumber, carbohydrate, carbohydrateAmount,
				meal, tester, health, exerciseDuration, exerciseIntensity,
				medication, medicationAmount, medicationUnit, HbA1c);
	}
}
