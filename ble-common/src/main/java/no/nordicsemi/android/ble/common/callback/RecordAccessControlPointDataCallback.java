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

package no.nordicsemi.android.ble.common.callback;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.RecordAccessControlPointCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Record Access Control Point callback that parses received data.
 * If the value does match characteristic specification the
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.record_access_control_point.xml
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public abstract class RecordAccessControlPointDataCallback extends ProfileReadResponse implements RecordAccessControlPointCallback {
	private final static int OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
	private final static int OP_CODE_RESPONSE_CODE = 6;
	private final static int OPERATOR_NULL = 0;
	private final static int RACP_RESPONSE_SUCCESS = 1;
	private final static int RACP_ERROR_NO_RECORDS_FOUND = 6;

	public RecordAccessControlPointDataCallback() {
		// empty
	}

	protected RecordAccessControlPointDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		if (data.size() < 3) {
			onInvalidDataReceived(device, data);
			return;
		}

		final int opCode = data.getIntValue(Data.FORMAT_UINT8, 0);
		if (opCode != OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE && opCode != OP_CODE_RESPONSE_CODE) {
			onInvalidDataReceived(device, data);
			return;
		}

		final int operator = data.getIntValue(Data.FORMAT_UINT8, 1);
		if (operator != OPERATOR_NULL) {
			onInvalidDataReceived(device, data);
			return;
		}

		switch (opCode) {
			case OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE: {
				// Field size is defined per service
				int numberOfRecords;

				switch (data.size() - 2) {
					case 1:
						numberOfRecords = data.getIntValue(Data.FORMAT_UINT8, 2);
						break;
					case 2:
						numberOfRecords = data.getIntValue(Data.FORMAT_UINT16, 2);
						break;
					case 4:
						numberOfRecords = data.getIntValue(Data.FORMAT_UINT32, 2);
						break;
					default:
						// Other field sizes are not supported
						onInvalidDataReceived(device, data);
						return;
				}
				onNumberOfRecordsReceived(device, numberOfRecords);
				break;
			}
			case OP_CODE_RESPONSE_CODE: {
				if (data.size() != 4) {
					onInvalidDataReceived(device, data);
					return;
				}

				final int requestCode = data.getIntValue(Data.FORMAT_UINT8, 2);
				final int responseCode = data.getIntValue(Data.FORMAT_UINT8, 3);
				if (responseCode == RACP_RESPONSE_SUCCESS) {
					onRecordAccessOperationCompleted(device, requestCode);
				} else if (responseCode == RACP_ERROR_NO_RECORDS_FOUND) {
					onRecordAccessOperationCompletedWithNoRecordsFound(device, requestCode);
				} else {
					onRecordAccessOperationError(device, requestCode, responseCode);
				}
				break;
			}
		}
	}
}
