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

package no.nordicsemi.android.ble.common.profile.cgm;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import no.nordicsemi.android.ble.common.profile.glucose.GlucoseSampleLocation;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseSampleType;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("unused")
public interface CGMSpecificOpsControlPointCallback extends CGMTypes {

	@Retention(RetentionPolicy.SOURCE)
	@IntDef(value = {
			CGM_OP_CODE_SET_COMMUNICATION_INTERVAL,
			CGM_OP_CODE_SET_CALIBRATION_VALUE,
			CGM_OP_CODE_SET_PATIENT_HIGH_ALERT_LEVEL,
			CGM_OP_CODE_SET_PATIENT_LOW_ALERT_LEVEL,
			CGM_OP_CODE_SET_HYPO_ALERT_LEVEL,
			CGM_OP_CODE_SET_HYPER_ALERT_LEVEL,
			CGM_OP_CODE_SET_RATE_OF_DECREASE_ALERT_LEVEL,
			CGM_OP_CODE_SET_RATE_OF_INCREASE_ALERT_LEVEL,
			CGM_OP_CODE_RESET_DEVICE_SPECIFIC_ERROR,
			CGM_OP_CODE_START_SESSION,
			CGM_OP_CODE_STOP_SESSION
	})
	@interface CGMOpCode {}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef(value = {
			CGM_ERROR_OP_CODE_NOT_SUPPORTED,
			CGM_ERROR_INVALID_OPERAND,
			CGM_ERROR_PROCEDURE_NOT_COMPLETED,
			CGM_ERROR_PARAMETER_OUT_OF_RANGE
	})
	@interface CGMErrorCode {}

	int CGM_OP_CODE_SET_COMMUNICATION_INTERVAL = 1;
	int CGM_OP_CODE_SET_CALIBRATION_VALUE = 4;
	int CGM_OP_CODE_SET_PATIENT_HIGH_ALERT_LEVEL = 7;
	int CGM_OP_CODE_SET_PATIENT_LOW_ALERT_LEVEL = 10;
	int CGM_OP_CODE_SET_HYPO_ALERT_LEVEL = 13;
	int CGM_OP_CODE_SET_HYPER_ALERT_LEVEL = 16;
	int CGM_OP_CODE_SET_RATE_OF_DECREASE_ALERT_LEVEL = 19;
	int CGM_OP_CODE_SET_RATE_OF_INCREASE_ALERT_LEVEL = 22;
	int CGM_OP_CODE_RESET_DEVICE_SPECIFIC_ERROR = 25;
	int CGM_OP_CODE_START_SESSION = 26;
	int CGM_OP_CODE_STOP_SESSION = 27;

	// int CGM_RESPONSE_SUCCESS = 1;
	int CGM_ERROR_OP_CODE_NOT_SUPPORTED = 2;
	int CGM_ERROR_INVALID_OPERAND = 3;
	int CGM_ERROR_PROCEDURE_NOT_COMPLETED = 4;
	int CGM_ERROR_PARAMETER_OUT_OF_RANGE = 5;

	/**
	 * Callback called when a CGM Specific Ops request has finished successfully.
	 *
	 * @param device      the target device.
	 * @param requestCode the request code that has completed. One of CGM_OP_CODE_* constants.
	 * @param secured     true, if the value received was secured with E2E-CRC value and the
	 *                    CRC matched the packet. False, if the CRC field was not present.
	 */
	void onCGMSpecificOpsOperationCompleted(@NonNull final BluetoothDevice device,
											@CGMOpCode final int requestCode,
											final boolean secured);

	/**
	 * Callback called when a CGM Specific Ops request has failed.
	 *
	 * @param device      the target device.
	 * @param requestCode the request code that has completed with an error.
	 *                    One of CGM_OP_CODE_* constants, or other if such was requested.
	 * @param errorCode   the received error code, see CGM_ERROR_* constants.
	 * @param secured     true, if the value received was secured with E2E-CRC value and the
	 *                    CRC matched the packet. False, if the CRC field was not present.
	 */
	void onCGMSpecificOpsOperationError(@NonNull final BluetoothDevice device,
										@CGMOpCode final int requestCode,
										@CGMErrorCode final int errorCode, final boolean secured);

	/**
	 * Callback called when a CGM Specific Ops response was received with and incorrect E2E CRC.
	 *
	 * @param device the target device.
	 * @param data   the CGM Specific Ops packet data that was received, including the CRC field.
	 */
	default void onCGMSpecificOpsResponseReceivedWithCrcError(@NonNull final BluetoothDevice device,
															  @NonNull final Data data) {
		// empty
	}

	/**
	 * Callback called as a result of 'Fet CGM communication interval' procedure.
	 *
	 * @param device   the target device.
	 * @param interval the time interval in minutes after which the CGM Measurement is sent to
	 *                 the client.
	 * @param secured  true, if the value received was secured with E2E-CRC value and
	 *                 the CRC matched the packet. False, if the CRC field was not present.
	 */
	default void onContinuousGlucoseCommunicationIntervalReceived(
			@NonNull final BluetoothDevice device,
			@IntRange(from = 0) final int interval,
			final boolean secured) {
		// empty
	}

	/**
	 * Callback called after a calibration value is received from the server.
	 *
	 * @param device                            the target device.
	 * @param glucoseConcentrationOfCalibration the glucose concentration value is transmitted in mg/dL.
	 * @param calibrationTime                   the time the calibration value has been measured as
	 *                                          relative offset to the Session Start Time in minutes.
	 * @param nextCalibrationTime               the relative offset to the Session Start Time when the
	 *                                          next calibration is required. A value of 0 means that
	 *                                          a calibration is required instantly.
	 * @param type                              the sample type, see TYPE_* constants.
	 * @param sampleLocation                    the sample location, see SAMPLE_LOCATION_* constants.
	 * @param calibrationDataRecordNumber       a unique number of the calibration record.
	 *                                          A value of 0 represents no calibration value is stored.
	 * @param status                            the status of the calibration procedure of the Server
	 *                                          related to the Calibration Data Record.
	 * @param secured                           true, if the value received was secured with E2E-CRC
	 *                                          value and the CRC matched the packet. False, if the
	 *                                          CRC field was not present.
	 */
	default void onContinuousGlucoseCalibrationValueReceived(
			@NonNull final BluetoothDevice device,
			@FloatRange(from = 0) final float glucoseConcentrationOfCalibration,
			@IntRange(from = 0, to = 65535) final int calibrationTime,
			@IntRange(from = 0, to = 65535) final int nextCalibrationTime,
			@GlucoseSampleType final int type,
			@GlucoseSampleLocation final int sampleLocation,
			@IntRange(from = 0, to = 65535) final int calibrationDataRecordNumber,
			@NonNull final CGMCalibrationStatus status,
			final boolean secured) {
		// empty
	}

	/**
	 * Callback called when the Patient High Alert response was received.
	 *
	 * @param device     the target device.
	 * @param alertLevel a level of glucose concentration in mg/dL to trigger the Patient High Alert
	 *                   in the Sensor Status Annunciation field.
	 * @param secured    true, if the value received was secured with E2E-CRC
	 *                   value and the CRC matched the packet. False, if the
	 *                   CRC field was not present.
	 */
	default void onContinuousGlucosePatientHighAlertReceived(
			@NonNull final BluetoothDevice device,
			@FloatRange(from = 0) final float alertLevel,
			final boolean secured) {
		// empty
	}

	/**
	 * Callback called when the Patient Low Alert response was received.
	 *
	 * @param device     the target device.
	 * @param alertLevel a level of glucose concentration in mg/dL to trigger the Patient Low Alert
	 *                   in the Sensor Status Annunciation field.
	 * @param secured    true, if the value received was secured with E2E-CRC
	 *                   value and the CRC matched the packet. False, if the
	 *                   CRC field was not present.
	 */
	default void onContinuousGlucosePatientLowAlertReceived(
			@NonNull final BluetoothDevice device,
			@FloatRange(from = 0) final float alertLevel,
			final boolean secured) {
		// empty
	}

	/**
	 * Callback called when the Hypo Alert response was received.
	 *
	 * @param device     the target device.
	 * @param alertLevel a level of glucose concentration in mg/dL to trigger the Hypo Alert
	 *                   in the Sensor Status Annunciation field.
	 * @param secured    true, if the value received was secured with E2E-CRC
	 *                   value and the CRC matched the packet. False, if the
	 *                   CRC field was not present.
	 */
	default void onContinuousGlucoseHypoAlertReceived(
			@NonNull final BluetoothDevice device,
			@FloatRange(from = 0) final float alertLevel,
			final boolean secured) {
		// empty
	}

	/**
	 * Callback called when the Hyper Alert response was received.
	 *
	 * @param device     the target device.
	 * @param alertLevel a level of glucose concentration in mg/dL to trigger the Hyper Alert
	 *                   in the Sensor Status Annunciation field.
	 * @param secured    true, if the value received was secured with E2E-CRC
	 *                   value and the CRC matched the packet. False, if the
	 *                   CRC field was not present.
	 */
	default void onContinuousGlucoseHyperAlertReceived(
			@NonNull final BluetoothDevice device,
			@FloatRange(from = 0) final float alertLevel,
			final boolean secured) {
		// empty
	}

	/**
	 * Callback called when the Rate Of Decrease Alert response was received.
	 *
	 * @param device     the target device.
	 * @param alertLevel a rate of glucose concentration change in mg/dL/min to trigger the
	 *                   Rate Of Decrease Alert in the Sensor Status Annunciation field.
	 * @param secured    true, if the value received was secured with E2E-CRC
	 *                   value and the CRC matched the packet. False, if the
	 *                   CRC field was not present.
	 */
	default void onContinuousGlucoseRateOfDecreaseAlertReceived(
			@NonNull final BluetoothDevice device,
			@FloatRange(from = 0) final float alertLevel,
			final boolean secured) {
		// empty
	}

	/**
	 * Callback called when the Hyper Alert response was received.
	 *
	 * @param device     the target device.
	 * @param alertLevel a rate of glucose concentration change in mg/dL/min to trigger the
	 *                   Rate Of Increase Alert in the Sensor Status Annunciation field.
	 * @param secured    true, if the value received was secured with E2E-CRC
	 *                   value and the CRC matched the packet. False, if the
	 *                   CRC field was not present.
	 */
	default void onContinuousGlucoseRateOfIncreaseAlertReceived(
			@NonNull final BluetoothDevice device,
			@FloatRange(from = 0) final float alertLevel,
			final boolean secured) {
		// empty
	}
}
