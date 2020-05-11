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

package no.nordicsemi.android.ble.common.profile.bp;

@SuppressWarnings("WeakerAccess")
public interface BloodPressureTypes {
	int UNIT_mmHg = 0;
	int UNIT_kPa = 1;

	class BPMStatus {
		public final boolean bodyMovementDetected;
		public final boolean cuffTooLose;
		public final boolean irregularPulseDetected;
		public final boolean pulseRateInRange;
		public final boolean pulseRateExceedsUpperLimit;
		public final boolean pulseRateIsLessThenLowerLimit;
		public final boolean improperMeasurementPosition;
		public final int value;

		public BPMStatus(final int status) {
			this.value = status;

			bodyMovementDetected = (status & 0x01) != 0;
			cuffTooLose = (status & 0x02) != 0;
			irregularPulseDetected = (status & 0x04) != 0;
			pulseRateInRange = (status & 0x18) >> 3 == 0;
			pulseRateExceedsUpperLimit = (status & 0x18) >> 3 == 1;
			pulseRateIsLessThenLowerLimit = (status & 0x18) >> 3 == 2;
			improperMeasurementPosition = (status & 0x20) != 0;
		}
	}

	/**
	 * Converts the value provided in given unit to mmHg.
	 * If the unit is already {@link #UNIT_mmHg} it will be returned as is.
	 *
	 * @param value the pressure value in given unit.
	 * @param unit the unit of the value ({@link #UNIT_mmHg} or {@link #UNIT_kPa}).
	 * @return Value in mmHg.
	 */
	static float toMmHg(final float value, @BloodPressureUnit final int unit) {
		if (unit == UNIT_mmHg) {
			return value;
		} else {
			return value / 0.133322387415f;
		}
	}

	/**
	 * Converts the value provided in given unit to kPa.
	 * If the unit is already {@link #UNIT_kPa} it will be returned as is.
	 *
	 * @param value the pressure value in given unit.
	 * @param unit the unit of the value ({@link #UNIT_mmHg} or {@link #UNIT_kPa}).
	 * @return Value in kPa.
	 */
	static float toKPa(final float value, @BloodPressureUnit final int unit) {
		if (unit == UNIT_kPa) {
			return value;
		} else {
			return value * 0.133322387415f;
		}
	}
}
