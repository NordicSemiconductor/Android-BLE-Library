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

package no.nordicsemi.android.ble.common.data;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

@SuppressWarnings("unused")
public final class RecordAccessControlPointData {
	private static final byte OP_CODE_REPORT_STORED_RECORDS = 1;
	private static final byte OP_CODE_DELETE_STORED_RECORDS = 2;
	private static final byte OP_CODE_ABORT_OPERATION = 3;
	private static final byte OP_CODE_REPORT_NUMBER_OF_RECORDS = 4;
	private static final byte OP_CODE_NUMBER_OF_STORED_RECORDS_RESPONSE = 5;
	private static final byte OP_CODE_RESPONSE_CODE = 6;

	private static final byte OPERATOR_NULL = 0;
	private static final byte OPERATOR_ALL_RECORDS = 1;
	private static final byte OPERATOR_LESS_THEN_OR_EQUAL = 2;
	private static final byte OPERATOR_GREATER_THEN_OR_EQUAL = 3;
	private static final byte OPERATOR_WITHING_RANGE = 4;
	private static final byte OPERATOR_FIRST_RECORD = 5;
	private static final byte OPERATOR_LAST_RECORD = 6;

	public enum FilterType {
		TIME_OFFSET(0x01),
		/** Alias of {@link #TIME_OFFSET} */
		SEQUENCE_NUMBER(0x01),
		USER_FACING_TIME(0x02);

		final byte type;

		FilterType(final int type) {
			this.type = (byte) type;
		}
	}

	private RecordAccessControlPointData() {
		// empty private constructor
	}

	public static Data reportAllStoredRecords() {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS);
	}

	public static Data reportFirstStoredRecord() {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_FIRST_RECORD);
	}

	public static Data reportLastStoredRecord() {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_LAST_RECORD);
	}

	public static Data reportStoredRecordsLessThenOrEqualTo(@NonNull final FilterType filter,
															@Data.IntFormat final int formatType,
															final int parameter) {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL,
				filter, formatType, parameter);
	}

	public static Data reportStoredRecordsGreaterThenOrEqualTo(@NonNull final FilterType filter,
															   @Data.IntFormat final int formatType,
															   final int parameter) {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL,
				filter, formatType, parameter);
	}

	public static Data reportStoredRecordsFromRange(@NonNull final FilterType filter,
													@Data.IntFormat final int formatType,
													final int start, final int end) {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_WITHING_RANGE,
				filter, formatType, start, end);
	}

	public static Data reportStoredRecordsLessThenOrEqualTo(@IntRange(from = 0) final int sequenceNumber) {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16, sequenceNumber);
	}

	public static Data reportStoredRecordsGreaterThenOrEqualTo(@IntRange(from = 0) final int sequenceNumber) {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16, sequenceNumber);
	}

	public static Data reportStoredRecordsFromRange(@IntRange(from = 0) final int startSequenceNumber,
													@IntRange(from = 0) final int endSequenceNumber) {
		return create(OP_CODE_REPORT_STORED_RECORDS, OPERATOR_WITHING_RANGE,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16,
				startSequenceNumber, endSequenceNumber);
	}

	public static Data deleteAllStoredRecords() {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_ALL_RECORDS);
	}

	public static Data deleteFirstStoredRecord() {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_FIRST_RECORD);
	}

	public static Data deleteLastStoredRecord() {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_LAST_RECORD);
	}

	public static Data deleteStoredRecordsLessThenOrEqualTo(@NonNull final FilterType filter,
															@Data.IntFormat final int formatType,
															final int parameter) {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL,
				filter, formatType, parameter);
	}

	public static Data deleteStoredRecordsGreaterThenOrEqualTo(@NonNull final FilterType filter,
															   @Data.IntFormat  int formatType,
															   final int parameter) {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL,
				filter, formatType, parameter);
	}

	public static Data deleteStoredRecordsFromRange(@NonNull final FilterType filter,
													@Data.IntFormat final int formatType,
													final int start, final int end) {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_WITHING_RANGE,
				filter, formatType, start, end);
	}

	public static Data deleteStoredRecordsLessThenOrEqualTo(@IntRange(from = 0) final int sequenceNumber) {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16, sequenceNumber);
	}

	public static Data deleteStoredRecordsGreaterThenOrEqualTo(@IntRange(from = 0) final int sequenceNumber) {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16, sequenceNumber);
	}

	public static Data deleteStoredRecordsFromRange(@IntRange(from = 0) final int startSequenceNumber,
													@IntRange(from = 0) final int endSequenceNumber) {
		return create(OP_CODE_DELETE_STORED_RECORDS, OPERATOR_WITHING_RANGE,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16,
				startSequenceNumber, endSequenceNumber);
	}

	public static Data reportNumberOfAllStoredRecords() {
		return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_ALL_RECORDS);
	}

	public static Data reportNumberOfStoredRecordsLessThenOrEqualTo(@NonNull final FilterType filter,
																	@Data.IntFormat final int formatType,
																	final int parameter) {
		return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL,
				filter, formatType, parameter);
	}

	public static Data reportNumberOfStoredRecordsGreaterThenOrEqualTo(@NonNull final FilterType filter,
																	   @Data.IntFormat final int formatType,
																	   final int parameter) {
		return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL,
				filter, formatType, parameter);
	}

	public static Data reportNumberOfStoredRecordsFromRange(@NonNull final FilterType filter,
															@Data.IntFormat final int formatType,
															final int start, final int end) {
		return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_WITHING_RANGE,
				filter, formatType, start, end);
	}

	public static Data reportNumberOfStoredRecordsLessThenOrEqualTo(@IntRange(from = 0) final int sequenceNumber) {
		return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_LESS_THEN_OR_EQUAL,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16, sequenceNumber);
	}

	public static Data reportNumberOfStoredRecordsGreaterThenOrEqualTo(@IntRange(from = 0) final int sequenceNumber) {
		return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_GREATER_THEN_OR_EQUAL,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16, sequenceNumber);
	}

	public static Data reportNumberOfStoredRecordsFromRange(@IntRange(from = 0) final int startSequenceNumber,
															@IntRange(from = 0) final int endSequenceNumber) {
		return create(OP_CODE_REPORT_NUMBER_OF_RECORDS, OPERATOR_WITHING_RANGE,
				FilterType.SEQUENCE_NUMBER, Data.FORMAT_UINT16,
				startSequenceNumber, endSequenceNumber);
	}

	public static Data abortOperation() {
		return create(OP_CODE_ABORT_OPERATION, OPERATOR_NULL);
	}

	private static Data create(final byte opCode, final byte operator) {
		return Data.opCode(opCode, operator);
	}

	private static Data create(final byte opCode, final byte operator,
							   @NonNull final FilterType filter,
							   @Data.IntFormat final int formatType,
							   final int... parameters) {
		final int parameterLen = formatType & 0x0F;

		final MutableData data = new MutableData(new byte[2 + 1 + parameters.length * parameterLen]);
		data.setByte(opCode, 0);
		data.setByte(operator, 1);
		if (parameters.length > 0) {
			data.setByte(filter.type, 2);
			data.setValue(parameters[0], formatType, 3);
		}
		if (parameters.length == 2) {
			data.setValue(parameters[1], formatType, 3 + parameterLen);
		}
		return data;
	}
}
