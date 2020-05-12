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
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public interface GlucoseMeasurementContextCallback {
	int UNIT_mg = 0;
	int UNIT_ml = 1;

	enum Carbohydrate {
		RESERVED(0),
		BREAKFAST(1),
		LUNCH(2),
		DINNER(3),
		SNACK(4),
		DRINK(5),
		SUPPER(6),
		BRUNCH(7);

		public final byte value;

		Carbohydrate(final int code) {
			this.value = (byte) code;
		}

		public static Carbohydrate from(final int code) {
			switch (code) {
				case 1:
					return BREAKFAST;
				case 2:
					return LUNCH;
				case 3:
					return DINNER;
				case 4:
					return SNACK;
				case 5:
					return DRINK;
				case 6:
					return SUPPER;
				case 7:
					return BRUNCH;
				default:
					return RESERVED;
			}
		}
	}

	enum Meal {
		RESERVED(0),
		PREPRANDIAL(1),
		POSTPRANDIAL(2),
		FASTING(3),
		CASUAL(4),
		BEDTIME(5);

		public final byte value;

		Meal(final int code) {
			this.value = (byte) code;
		}

		public static Meal from(final int code) {
			switch (code) {
				case 1:
					return PREPRANDIAL;
				case 2:
					return POSTPRANDIAL;
				case 3:
					return FASTING;
				case 4:
					return CASUAL;
				case 5:
					return BEDTIME;
				default:
					return RESERVED;
			}
		}
	}

	enum Tester {
		RESERVED(0),
		SELF(1),
		HEALTH_CARE_PROFESSIONAL(2),
		LAB_TEST(3),
		NOT_AVAILABLE(15);

		public final byte value;

		Tester(final int code) {
			this.value = (byte) code;
		}

		public static Tester from(final int code) {
			switch (code) {
				case 1:
					return SELF;
				case 2:
					return HEALTH_CARE_PROFESSIONAL;
				case 3:
					return LAB_TEST;
				case 15:
					return NOT_AVAILABLE;
				default:
					return RESERVED;
			}
		}
	}

	enum Health {
		RESERVED(0),
		MINOR_HEALTH_ISSUES(1),
		MAJOR_HEALTH_ISSUES(2),
		DURING_MENSES(3),
		UNDER_STRESS(4),
		NO_HEALTH_ISSUES(5),
		NOT_AVAILABLE(15);

		public final byte value;

		Health(final int code) {
			this.value = (byte) code;
		}

		public static Health from(final int code) {
			switch (code) {
				case 1:
					return MINOR_HEALTH_ISSUES;
				case 2:
					return MAJOR_HEALTH_ISSUES;
				case 3:
					return DURING_MENSES;
				case 4:
					return UNDER_STRESS;
				case 5:
					return NO_HEALTH_ISSUES;
				case 15:
					return NOT_AVAILABLE;
				default:
					return RESERVED;
			}
		}
	}

	enum Medication {
		RESERVED(0),
		RAPID_ACTING_INSULIN(1),
		SHORT_ACTING_INSULIN(2),
		INTERMEDIATE_ACTING_INSULIN(3),
		LONG_ACTING_INSULIN(4),
		PRE_MIXED_INSULIN(5);

		public final byte value;

		Medication(final int code) {
			this.value = (byte) code;
		}

		public static Medication from(final int code) {
			switch (code) {
				case 1:
					return RAPID_ACTING_INSULIN;
				case 2:
					return SHORT_ACTING_INSULIN;
				case 3:
					return INTERMEDIATE_ACTING_INSULIN;
				case 4:
					return LONG_ACTING_INSULIN;
				case 5:
					return PRE_MIXED_INSULIN;
				default:
					return RESERVED;
			}
		}
	}

	/**
	 * Callback called when Glucose Measurement Context value was received.
	 *
	 * @param device             the target device.
	 * @param sequenceNumber     the sequence number that matches the Glucose Measurement
	 *                           sequence number.
	 * @param carbohydrate       an optional carbohydrate ID.
	 * @param carbohydrateAmount amount of carbohydrate in grams.
	 * @param meal               an optional meal ID.
	 * @param tester             an optional tester ID.
	 * @param health             an optional health information.
	 * @param exerciseDuration   exercise duration in seconds. Value 65535 means an overrun.
	 * @param exerciseIntensity  exercise intensity in percent.
	 * @param medication         an optional medication ID.
	 * @param medicationAmount   amount of medication in milligrams or milliliters,
	 *                           depending on the medicationUnit value.
	 * @param medicationUnit     the unit of medication amount ({@link #UNIT_mg} or {@link #UNIT_ml}).
	 * @param HbA1c              the amount of glycated haemoglobin, in percentage.
	 */
	void onGlucoseMeasurementContextReceived(@NonNull final BluetoothDevice device,
											 @IntRange(from = 0, to = 65535) final int sequenceNumber,
											 @Nullable final Carbohydrate carbohydrate,
											 @Nullable final Float carbohydrateAmount,
											 @Nullable final Meal meal,
											 @Nullable final Tester tester,
											 @Nullable final Health health,
											 @Nullable @IntRange(from = 0, to = 65535) final Integer exerciseDuration,
											 @Nullable @IntRange(from = 0, to = 100) final Integer exerciseIntensity,
											 @Nullable final Medication medication,
											 @Nullable final Float medicationAmount,
											 @Nullable @MedicationUnit final Integer medicationUnit,
											 @Nullable @FloatRange(from = 0.0f, to = 100.0f) final Float HbA1c);
}
