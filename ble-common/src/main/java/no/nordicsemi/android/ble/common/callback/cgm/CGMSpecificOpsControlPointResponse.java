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
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     CGMSpecificOpsControlPointResponse response = waitForIndication(characteristic)
 *           .trigger(CGMSpecificOpsControlPointData.startSession())
 *           .awaitValid(CGMSpecificOpsControlPointResponse.class);
 *     if (response.isOperationCompleted()) {
 *         ...
 *     }
 *     ...
 * } catch ({@link RequestFailedException} e) {
 *     Log.w(TAG, "Request failed with status " + e.getStatus(), e);
 * } catch ({@link InvalidDataException} e) {
 *     Log.w(TAG, "Invalid data received: " + e.getResponse().getRawData());
 * }
 * </pre>
 * </p>
 */
@SuppressWarnings("unused")
public final class CGMSpecificOpsControlPointResponse extends CGMSpecificOpsControlPointDataCallback implements CRCSecuredResponse, Parcelable {
	private boolean operationCompleted;
	private boolean secured;
	private boolean crcValid;
	private int requestCode;
	private int errorCode;
	private int glucoseCommunicationInterval;
	private float glucoseConcentrationOfCalibration;
	private int calibrationTime;
	private int nextCalibrationTime;
	private int type;
	private int sampleLocation;
	private int calibrationDataRecordNumber;
	private CGMCalibrationStatus calibrationStatus;
	private float alertLevel;

	public CGMSpecificOpsControlPointResponse() {
		// empty
	}

	@Override
	public void onCGMSpecificOpsOperationCompleted(@NonNull final BluetoothDevice device, final int requestCode, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = requestCode;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onCGMSpecificOpsOperationError(@NonNull final BluetoothDevice device, final int requestCode, final int errorCode, final boolean secured) {
		this.operationCompleted = false;
		this.requestCode = requestCode;
		this.errorCode = errorCode;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onCGMSpecificOpsResponseReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		onInvalidDataReceived(device, data);
		this.operationCompleted = false;
		this.secured = true;
		this.crcValid = false;
	}

	@Override
	public void onContinuousGlucoseCommunicationIntervalReceived(@NonNull final BluetoothDevice device, final int interval, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_COMMUNICATION_INTERVAL;
		this.glucoseCommunicationInterval = interval;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseCalibrationValueReceived(@NonNull final BluetoothDevice device, final float glucoseConcentrationOfCalibration,
															final int calibrationTime, final int nextCalibrationTime,
															final int type, final int sampleLocation, final int calibrationDataRecordNumber,
															@NonNull final CGMCalibrationStatus status, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_CALIBRATION_VALUE;
		this.glucoseConcentrationOfCalibration = glucoseConcentrationOfCalibration;
		this.calibrationTime = calibrationTime;
		this.nextCalibrationTime = nextCalibrationTime;
		this.type = type;
		this.sampleLocation = sampleLocation;
		this.calibrationDataRecordNumber = calibrationDataRecordNumber;
		this.calibrationStatus = status;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucosePatientHighAlertReceived(@NonNull final BluetoothDevice device, final float alertLevel, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_PATIENT_HIGH_ALERT_LEVEL;
		this.alertLevel = alertLevel;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucosePatientLowAlertReceived(@NonNull final BluetoothDevice device, final float alertLevel, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_PATIENT_LOW_ALERT_LEVEL;
		this.alertLevel = alertLevel;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseHypoAlertReceived(@NonNull final BluetoothDevice device, final float alertLevel, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_HYPO_ALERT_LEVEL;
		this.alertLevel = alertLevel;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseHyperAlertReceived(@NonNull final BluetoothDevice device, final float alertLevel, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_HYPER_ALERT_LEVEL;
		this.alertLevel = alertLevel;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseRateOfDecreaseAlertReceived(@NonNull final BluetoothDevice device, final float alertLevel, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_RATE_OF_DECREASE_ALERT_LEVEL;
		this.alertLevel = alertLevel;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseRateOfIncreaseAlertReceived(@NonNull final BluetoothDevice device, final float alertLevel, final boolean secured) {
		this.operationCompleted = true;
		this.requestCode = CGM_OP_CODE_SET_RATE_OF_INCREASE_ALERT_LEVEL;
		this.alertLevel = alertLevel;
		this.secured = secured;
		this.crcValid = secured;
	}

	public boolean isOperationCompleted() {
		return operationCompleted;
	}

	@Override
	public boolean isSecured() {
		return secured;
	}

	@Override
	public boolean isCrcValid() {
		return crcValid;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public int getGlucoseCommunicationInterval() {
		return glucoseCommunicationInterval;
	}

	public float getGlucoseConcentrationOfCalibration() {
		return glucoseConcentrationOfCalibration;
	}

	public int getCalibrationTime() {
		return calibrationTime;
	}

	public int getNextCalibrationTime() {
		return nextCalibrationTime;
	}

	public int getType() {
		return type;
	}

	public int getSampleLocation() {
		return sampleLocation;
	}

	public int getCalibrationDataRecordNumber() {
		return calibrationDataRecordNumber;
	}

	public CGMCalibrationStatus getCalibrationStatus() {
		return calibrationStatus;
	}

	public float getAlertLevel() {
		return alertLevel;
	}

	// Parcelable
	private CGMSpecificOpsControlPointResponse(final Parcel in) {
		super(in);
		operationCompleted = in.readByte() != 0;
		secured = in.readByte() != 0;
		crcValid = in.readByte() != 0;
		requestCode = in.readInt();
		errorCode = in.readInt();
		glucoseCommunicationInterval = in.readInt();
		glucoseConcentrationOfCalibration = in.readFloat();
		calibrationTime = in.readInt();
		nextCalibrationTime = in.readInt();
		type = in.readInt();
		sampleLocation = in.readInt();
		calibrationDataRecordNumber = in.readInt();
		if (in.readByte() == 0) {
			calibrationStatus = null;
		} else {
			calibrationStatus = new CGMCalibrationStatus(in.readInt());
		}
		alertLevel = in.readFloat();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeByte((byte) (operationCompleted ? 1 : 0));
		dest.writeByte((byte) (secured ? 1 : 0));
		dest.writeByte((byte) (crcValid ? 1 : 0));
		dest.writeInt(requestCode);
		dest.writeInt(errorCode);
		dest.writeInt(glucoseCommunicationInterval);
		dest.writeFloat(glucoseConcentrationOfCalibration);
		dest.writeInt(calibrationTime);
		dest.writeInt(nextCalibrationTime);
		dest.writeInt(type);
		dest.writeInt(sampleLocation);
		dest.writeInt(calibrationDataRecordNumber);
		if (calibrationStatus == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(calibrationStatus.value);
		}
		dest.writeFloat(alertLevel);
	}

	public static final Creator<CGMSpecificOpsControlPointResponse> CREATOR = new Creator<CGMSpecificOpsControlPointResponse>() {
		@Override
		public CGMSpecificOpsControlPointResponse createFromParcel(final Parcel in) {
			return new CGMSpecificOpsControlPointResponse(in);
		}

		@Override
		public CGMSpecificOpsControlPointResponse[] newArray(final int size) {
			return new CGMSpecificOpsControlPointResponse[size];
		}
	};
}
