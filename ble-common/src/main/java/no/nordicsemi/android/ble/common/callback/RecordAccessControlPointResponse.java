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
import android.os.Parcelable;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     RecordAccessControlPointResponse response = readCharacteristic(characteristic)
 *           .awaitValid(RecordAccessControlPointResponse.class);
 *     if (response.isOperationCompleted() && response.wereRecordsFound()) {
 *         int number = response.getNumberOfRecords();
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
public final class RecordAccessControlPointResponse extends RecordAccessControlPointDataCallback implements Parcelable {
	private boolean operationCompleted;
	private boolean recordsFound;
	private int numberOfRecords = -1;
	private int errorCode;
	private int requestCode;

	public RecordAccessControlPointResponse() {
		// empty
	}

	@Override
	public void onRecordAccessOperationCompleted(@NonNull final BluetoothDevice device, final int requestCode) {
		this.operationCompleted = true;
		this.recordsFound = true;
		this.requestCode = requestCode;
	}

	@Override
	public void onRecordAccessOperationCompletedWithNoRecordsFound(@NonNull final BluetoothDevice device, final int requestCode) {
		this.operationCompleted = true;
		this.numberOfRecords = 0;
		this.recordsFound = false;
		this.requestCode = requestCode;
	}

	@Override
	public void onNumberOfRecordsReceived(@NonNull final BluetoothDevice device, final int numberOfRecords) {
		this.operationCompleted = true;
		this.numberOfRecords = numberOfRecords;
		this.recordsFound = numberOfRecords > 0;
		this.requestCode = RACP_OP_CODE_REPORT_NUMBER_OF_RECORDS;
	}

	@Override
	public void onRecordAccessOperationError(@NonNull final BluetoothDevice device, final int requestCode, final int errorCode) {
		this.operationCompleted = false;
		this.errorCode = errorCode;
		this.requestCode = requestCode;
	}

	/**
	 * Returns the request Op Code. One of {@link #RACP_OP_CODE_REPORT_STORED_RECORDS},
	 * {@link #RACP_OP_CODE_DELETE_STORED_RECORDS}, {@link #RACP_OP_CODE_DELETE_STORED_RECORDS} or
	 * {@link #RACP_OP_CODE_REPORT_NUMBER_OF_RECORDS}.
	 *
	 * @return The request Op Code.
	 */
	public int getRequestCode() {
		return requestCode;
	}

	/**
	 * Returns true if the operation has completed successfully.
	 *
	 * @return True in case of success, false if an error was reported. The error code can be
	 * obtained using {@link #getErrorCode()}.
	 */
	public boolean isOperationCompleted() {
		return operationCompleted;
	}

	/**
	 * Returns false if operation completed with error {@link #RACP_ERROR_NO_RECORDS_FOUND},
	 * true in other cases.
	 *
	 * @return True if records were found.
	 */
	public boolean wereRecordsFound() {
		return recordsFound;
	}

	/**
	 * Returns number of records found matching filter criteria. This is only valid if
	 * {@link #RACP_OP_CODE_REPORT_NUMBER_OF_RECORDS} request was made.
	 *
	 * @return Number of records or -1 if not requested.
	 */
	public int getNumberOfRecords() {
		return numberOfRecords;
	}

	/**
	 * Returned error code. Check RACP_ERROR_* constants.
	 *
	 * @return The error code.
	 */
	public int getErrorCode() {
		return errorCode;
	}

	// Parcelable
	private RecordAccessControlPointResponse(final Parcel in) {
		super(in);
		operationCompleted = in.readByte() != 0;
		recordsFound = in.readByte() != 0;
		numberOfRecords = in.readInt();
		errorCode = in.readInt();
		requestCode = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeByte((byte) (operationCompleted ? 1 : 0));
		dest.writeByte((byte) (recordsFound ? 1 : 0));
		dest.writeInt(numberOfRecords);
		dest.writeInt(errorCode);
		dest.writeInt(requestCode);
	}

	public static final Creator<RecordAccessControlPointResponse> CREATOR = new Creator<RecordAccessControlPointResponse>() {
		@Override
		public RecordAccessControlPointResponse createFromParcel(final Parcel in) {
			return new RecordAccessControlPointResponse(in);
		}

		@Override
		public RecordAccessControlPointResponse[] newArray(final int size) {
			return new RecordAccessControlPointResponse[size];
		}
	};
}
