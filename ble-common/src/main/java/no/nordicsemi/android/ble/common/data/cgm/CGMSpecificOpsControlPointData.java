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

package no.nordicsemi.android.ble.common.data.cgm;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

import no.nordicsemi.android.ble.common.profile.cgm.CGMTypes;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseSampleLocation;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseSampleType;
import no.nordicsemi.android.ble.common.util.CRC16;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

@SuppressWarnings("unused")
public final class CGMSpecificOpsControlPointData implements CGMTypes {
	private static final byte OP_CODE_SET_COMMUNICATION_INTERVAL = 1;
	private static final byte OP_CODE_GET_COMMUNICATION_INTERVAL = 2;
	private static final byte OP_CODE_SET_CALIBRATION_VALUE = 4;
	private static final byte OP_CODE_GET_CALIBRATION_VALUE = 5;
	private static final byte OP_CODE_SET_PATIENT_HIGH_ALERT_LEVEL = 7;
	private static final byte OP_CODE_GET_PATIENT_HIGH_ALERT_LEVEL = 8;
	private static final byte OP_CODE_SET_PATIENT_LOW_ALERT_LEVEL = 10;
	private static final byte OP_CODE_GET_PATIENT_LOW_ALERT_LEVEL = 11;
	private static final byte OP_CODE_SET_HYPO_ALERT_LEVEL = 13;
	private static final byte OP_CODE_GET_HYPO_ALERT_LEVEL = 14;
	private static final byte OP_CODE_SET_HYPER_ALERT_LEVEL = 16;
	private static final byte OP_CODE_GET_HYPER_ALERT_LEVEL = 17;
	private static final byte OP_CODE_SET_RATE_OF_DECREASE_ALERT_LEVEL = 19;
	private static final byte OP_CODE_GET_RATE_OF_DECREASE_ALERT_LEVEL = 20;
	private static final byte OP_CODE_SET_RATE_OF_INCREASE_ALERT_LEVEL = 22;
	private static final byte OP_CODE_GET_RATE_OF_INCREASE_ALERT_LEVEL = 23;
	private static final byte OP_CODE_RESET_DEVICE_SPECIFIC_ERROR = 25;
	private static final byte OP_CODE_START_SESSION = 26;
	private static final byte OP_CODE_STOP_SESSION = 27;

	private CGMSpecificOpsControlPointData() {
		// empty private constructor
	}

	public static Data startSession(final boolean secure) {
		return create(OP_CODE_START_SESSION, secure);
	}

	public static Data stopSession(final boolean secure) {
		return create(OP_CODE_STOP_SESSION, secure);
	}

	public static Data resetDeviceSpecificAlert(final boolean secure) {
		return create(OP_CODE_RESET_DEVICE_SPECIFIC_ERROR, secure);
	}

	public static Data setCommunicationInterval(@IntRange(from = 0, to = 65535) final int interval,
												final boolean secure) {
		return create(OP_CODE_SET_COMMUNICATION_INTERVAL, interval, Data.FORMAT_UINT8, secure);
	}

	public static Data setCommunicationIntervalToFastestSupported(final boolean secure) {
		return create(OP_CODE_SET_COMMUNICATION_INTERVAL, 0xFF, Data.FORMAT_UINT8, secure);
	}

	public static Data disablePeriodicCommunication(final boolean secure) {
		return create(OP_CODE_SET_COMMUNICATION_INTERVAL, 0xFF, Data.FORMAT_UINT8, secure);
	}

	public static Data getCommunicationInterval(final boolean secure) {
		return create(OP_CODE_GET_COMMUNICATION_INTERVAL, secure);
	}

	public static Data setCalibrationValue(@FloatRange(from = 0) final float glucoseConcentrationOfCalibration,
										   @GlucoseSampleType final int sampleType,
										   @GlucoseSampleLocation final int sampleLocation,
										   @IntRange(from = 0, to = 65535) final int calibrationTime,
										   @IntRange(from = 0, to = 65535) final int nextCalibrationTime,
										   final boolean secure) {
		final MutableData data = new MutableData(new byte[11 + (secure ? 2 : 0)]);
		data.setByte(OP_CODE_SET_CALIBRATION_VALUE, 0);
		data.setValue(glucoseConcentrationOfCalibration, Data.FORMAT_SFLOAT, 1);
		data.setValue(calibrationTime, Data.FORMAT_UINT16, 3);
		final int typeAndSampleLocation = ((sampleLocation & 0xF) << 8) | (sampleType & 0xF);
		data.setValue(typeAndSampleLocation, Data.FORMAT_UINT8, 5);
		data.setValue(nextCalibrationTime, Data.FORMAT_UINT16, 6);
		data.setValue(0, Data.FORMAT_UINT16, 8); // ignored: calibration data record number
		data.setValue(0, Data.FORMAT_UINT8, 10); // ignored: calibration status
		return appendCrc(data, secure);
	}

	public static Data getCalibrationValue(@IntRange(from = 0) final int calibrationDataRecordNumber, final boolean secure) {
		return create(OP_CODE_GET_CALIBRATION_VALUE, calibrationDataRecordNumber, Data.FORMAT_UINT16, secure);
	}

	public static Data getLastCalibrationValue(final boolean secure) {
		return create(OP_CODE_GET_CALIBRATION_VALUE, 0xFFFF, Data.FORMAT_UINT16, secure);
	}

	public static Data setPatientHighAlertLevel(@FloatRange(from = 0) final float level,
												final boolean secure) {
		return create(OP_CODE_SET_PATIENT_HIGH_ALERT_LEVEL, level, secure);
	}

	public static Data getPatientHighAlertLevel(final boolean secure) {
		return create(OP_CODE_GET_PATIENT_HIGH_ALERT_LEVEL, secure);
	}

	public static Data setPatientLowAlertLevel(@FloatRange(from = 0) final float level,
											   final boolean secure) {
		return create(OP_CODE_SET_PATIENT_LOW_ALERT_LEVEL, level, secure);
	}

	public static Data getPatientLowAlertLevel(final boolean secure) {
		return create(OP_CODE_GET_PATIENT_LOW_ALERT_LEVEL, secure);
	}

	public static Data setHypoAlertLevel(@FloatRange(from = 0) final float level,
										 final boolean secure) {
		return create(OP_CODE_SET_HYPO_ALERT_LEVEL, level, secure);
	}

	public static Data getHypoAlertLevel(final boolean secure) {
		return create(OP_CODE_GET_HYPO_ALERT_LEVEL, secure);
	}

	public static Data setHyperAlertLevel(@FloatRange(from = 0) final float level,
										  final boolean secure) {
		return create(OP_CODE_SET_HYPER_ALERT_LEVEL, level, secure);
	}

	public static Data getHyperAlertLevel(final boolean secure) {
		return create(OP_CODE_GET_HYPER_ALERT_LEVEL, secure);
	}

	public static Data setRateOfDecreaseAlertLevel(@FloatRange(from = 0) final float level,
												   final boolean secure) {
		return create(OP_CODE_SET_RATE_OF_DECREASE_ALERT_LEVEL, level, secure);
	}

	public static Data getRateOfDecreaseAlertLevel(final boolean secure) {
		return create(OP_CODE_GET_RATE_OF_DECREASE_ALERT_LEVEL, secure);
	}

	public static Data setRateOfIncreaseAlertLevel(@FloatRange(from = 0) final float level,
												   final boolean secure) {
		return create(OP_CODE_SET_RATE_OF_INCREASE_ALERT_LEVEL, level, secure);
	}

	public static Data getRateOfIncreaseAlertLevel(final boolean secure) {
		return create(OP_CODE_GET_RATE_OF_INCREASE_ALERT_LEVEL, secure);
	}

	private static Data create(final byte opCode, final boolean secure) {
		final MutableData data = new MutableData(new byte[1 + (secure ? 2 : 0)]);
		data.setByte(opCode, 0);
		return appendCrc(data, secure);
	}

	private static Data create(final byte opCode, final int value, final int format, final boolean secure) {
		final MutableData data = new MutableData(new byte[1 + (format & 0xF) + (secure ? 2 : 0)]);
		data.setByte(opCode, 0);
		data.setValue(value, format, 1);
		return appendCrc(data, secure);
	}

	private static Data create(final byte opCode, final float value, final boolean secure) {
		final MutableData data = new MutableData(new byte[3 + (secure ? 2 : 0)]);
		data.setByte(opCode, 0);
		data.setValue(value, Data.FORMAT_SFLOAT, 1);
		return appendCrc(data, secure);
	}

	@SuppressWarnings("ConstantConditions")
	private static Data appendCrc(final MutableData data, final boolean secure) {
		if (secure) {
			final int length = data.size() - 2;
			final int crc = CRC16.MCRF4XX(data.getValue(), 0, length);
			data.setValue(crc, Data.FORMAT_UINT16, length);
		}
		return data;
	}
}
