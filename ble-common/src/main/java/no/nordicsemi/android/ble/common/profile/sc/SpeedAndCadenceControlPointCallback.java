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

package no.nordicsemi.android.ble.common.profile.sc;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unused")
public interface SpeedAndCadenceControlPointCallback extends SensorLocationTypes {

	@Retention(RetentionPolicy.SOURCE)
	@IntDef(value = {
			SC_OP_CODE_SET_CUMULATIVE_VALUE,
			SC_OP_CODE_START_SENSOR_CALIBRATION,
			SC_OP_CODE_UPDATE_SENSOR_LOCATION,
			SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS
	})
	@interface SCOpCode {}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef(value = {
			SC_ERROR_OP_CODE_NOT_SUPPORTED,
			SC_ERROR_INVALID_PARAMETER,
			SC_ERROR_OPERATION_FAILED
	})
	@interface SCErrorCode {}

	int SC_OP_CODE_SET_CUMULATIVE_VALUE = 1;
	int SC_OP_CODE_START_SENSOR_CALIBRATION = 2;
	int SC_OP_CODE_UPDATE_SENSOR_LOCATION = 3;
	int SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS = 4;

	// int SC_RESPONSE_SUCCESS = 1;
	int SC_ERROR_OP_CODE_NOT_SUPPORTED = 2;
	int SC_ERROR_INVALID_PARAMETER = 3;
	int SC_ERROR_OPERATION_FAILED = 4;

	/**
	 * Callback called when a SC Control Point request has finished successfully.
	 * In case of {@link #SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS} request, the
	 * {@link #onSupportedSensorLocationsReceived(BluetoothDevice, int[])} will be called instead.
	 *
	 * @param device      the target device.
	 * @param requestCode the request code that has completed. One of SC_OP_CODE_* constants.
	 */
	void onSCOperationCompleted(@NonNull final BluetoothDevice device,
								@SCOpCode final int requestCode);

	/**
	 * Callback called when a SC Control Point request has failed.
	 *
	 * @param device      the target device.
	 * @param requestCode the request code that has completed with an error.
	 *                    One of SC_OP_CODE_* constants, or other if such was requested.
	 * @param errorCode   the received error code, see SC_ERROR_* constants.
	 */
	void onSCOperationError(@NonNull final BluetoothDevice device,
							@SCOpCode final int requestCode,
							@SCErrorCode final int errorCode);

	/**
	 * Callback indicating successful response for
	 * {@link #SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS} request.
	 *
	 * @param device    the target device.
	 * @param locations an array with supported locations. See SENSOR_LOCATION_* constants.
	 */
	void onSupportedSensorLocationsReceived(@NonNull final BluetoothDevice device,
											@NonNull final int[] locations);
}
