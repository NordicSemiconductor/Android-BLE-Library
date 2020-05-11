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
import androidx.annotation.NonNull;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class RecordAccessControlPointDataCallbackTest {
	private boolean success;
	private boolean successNoRecords;
	private boolean invalidData;
	private int requestCode;
	private int error;
	private int numberOfRecords;

	private final DataReceivedCallback callback = new RecordAccessControlPointDataCallback() {
		@Override
		public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			// Reset flags
			RecordAccessControlPointDataCallbackTest.this.success = false;
			RecordAccessControlPointDataCallbackTest.this.successNoRecords = false;
			RecordAccessControlPointDataCallbackTest.this.invalidData = false;
			RecordAccessControlPointDataCallbackTest.this.requestCode = 0;
			RecordAccessControlPointDataCallbackTest.this.error = 0;
			RecordAccessControlPointDataCallbackTest.this.numberOfRecords = 0;

			super.onDataReceived(device, data);
		}

		@Override
		public void onRecordAccessOperationCompleted(@NonNull final BluetoothDevice device, final int requestCode) {
			RecordAccessControlPointDataCallbackTest.this.success = true;
			RecordAccessControlPointDataCallbackTest.this.requestCode = requestCode;
		}

		@Override
		public void onRecordAccessOperationCompletedWithNoRecordsFound(@NonNull final BluetoothDevice device, final int requestCode) {
			RecordAccessControlPointDataCallbackTest.this.successNoRecords = true;
			RecordAccessControlPointDataCallbackTest.this.requestCode = requestCode;
		}

		@Override
		public void onNumberOfRecordsReceived(@NonNull final BluetoothDevice device, final int numberOfRecords) {
			RecordAccessControlPointDataCallbackTest.this.numberOfRecords = numberOfRecords;
		}

		@Override
		public void onRecordAccessOperationError(@NonNull final BluetoothDevice device, final int requestCode, final int errorCode) {
			RecordAccessControlPointDataCallbackTest.this.error = errorCode;
			RecordAccessControlPointDataCallbackTest.this.requestCode = requestCode;
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			RecordAccessControlPointDataCallbackTest.this.invalidData = true;
		}
	};

	@Test
	public void onRecordAccessOperationCompleted() {
		final Data data = new Data(new byte[] { 6, 0, 1, 1 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertEquals(1, requestCode);
	}

	@Test
	public void onRecordAccessOperationError_opCodeNotSupported() {
		final Data data = new Data(new byte[] { 6, 0, 12, 2 });
		callback.onDataReceived(null, data);
		assertEquals(2, error);
		assertEquals(12, requestCode);
	}

	@Test
	public void onRecordAccessOperationError_invalidOperator() {
		final Data data = new Data(new byte[] { 6, 0, 1, 3 });
		callback.onDataReceived(null, data);
		assertEquals(error, 3);
		assertEquals(1, requestCode);
	}

	@Test
	public void onRecordAccessOperationError_operatorNotSupported() {
		final Data data = new Data(new byte[] { 6, 0, 2, 4 });
		callback.onDataReceived(null, data);
		assertEquals(4, error);
		assertEquals(2, requestCode);
	}

	@Test
	public void onRecordAccessOperationError_invalidOperand() {
		final Data data = new Data(new byte[] { 6, 0, 1, 5 });
		callback.onDataReceived(null, data);
		assertEquals(5, error);
	}

	@Test
	public void onRecordAccessOperationCompletedWithNoRecords() {
		final Data data = new Data(new byte[] { 6, 0, 1, 6 });
		callback.onDataReceived(null, data);
		assertTrue(successNoRecords);
	}

	@Test
	public void onRecordAccessOperationError_abortUnsuccessful() {
		final Data data = new Data(new byte[] { 6, 0, 3, 7 });
		callback.onDataReceived(null, data);
		assertEquals(7, error);
	}

	@Test
	public void onRecordAccessOperationError_procedureNotCompleted() {
		final Data data = new Data(new byte[] { 6, 0, 2, 8 });
		callback.onDataReceived(null, data);
		assertEquals(8, error);
	}

	@Test
	public void onRecordAccessOperationError_operandNotSupported() {
		final Data data = new Data(new byte[] { 6, 0, 2, 9 });
		callback.onDataReceived(null, data);
		assertEquals(9, error);
	}

	@Test
	public void onRecordAccessOperationError_unknownErrorCode() {
		final Data data = new Data(new byte[] { 6, 0, 1, 10 });
		callback.onDataReceived(null, data);
		assertEquals(10, error);
	}

	@Test
	public void onNumberOfRecordsReceived_uint8() {
		final Data data = new Data(new byte[] { 5, 0, 1 });
		callback.onDataReceived(null, data);
		assertEquals(numberOfRecords, 1);
	}

	@Test
	public void onNumberOfRecordsReceived_uint16() {
		final Data data = new Data(new byte[] { 5, 0, 2, 1 });
		callback.onDataReceived(null, data);
		assertEquals(numberOfRecords, 258);
	}

	@Test
	public void onNumberOfRecordsReceived_uint32() {
		final Data data = new Data(new byte[] { 5, 0, 4, 3, 2, 1 });
		callback.onDataReceived(null, data);
		assertEquals(numberOfRecords, 16909060);
	}

	@Test
	public void onNumberOfRecordsReceived_unsupportedLength() {
		final Data data = new Data(new byte[] { 5, 0, 3, 2, 1 });
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}

	@Test
	public void onInvalidDataReceived_tooShort() {
		final Data data = new Data(new byte[] { 1, 1 });
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}

	@Test
	public void onInvalidDataReceived_wrongOpCode() {
		final Data data = new Data(new byte[] { 1, 3, 1, 2, 0 });
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}

	@Test
	public void onInvalidDataReceived_invalidOperator() {
		final Data data = new Data(new byte[] { 6, 1, 1 });
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}
}