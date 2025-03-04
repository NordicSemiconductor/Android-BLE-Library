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
package no.nordicsemi.android.ble.error;

import android.bluetooth.BluetoothGatt;

/**
 * Parses the GATT and HCI errors to human readable strings.
 * <p>
 * See: <a href="https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/master/stack/include/gatt_api.h">gatt_api.h</a> for details.<br>
 * See also: <a href="https://android.googlesource.com/platform/external/libnfc-nci/+/master/src/include/hcidefs.h#447">hcidefs.h</a> for other possible HCI errors.
 */
@SuppressWarnings("WeakerAccess")
public class GattError {
	public static final int GATT_SUCCESS = BluetoothGatt.GATT_SUCCESS;
	public static final int GATT_CONN_L2C_FAILURE = 0x01;
	public static final int GATT_CONN_TIMEOUT = 0x08;
	public static final int GATT_CONN_TERMINATE_PEER_USER = 0x13;
	public static final int GATT_CONN_TERMINATE_LOCAL_HOST = 0x16;
	public static final int GATT_CONN_FAIL_ESTABLISH = 0x3E;
	public static final int GATT_CONN_LMP_TIMEOUT = 0x22;
	public static final int GATT_CONN_CANCEL = 0x0100;
	public static final int GATT_ERROR = 0x0085; // Device not reachable

	public static final int GATT_INVALID_HANDLE = 0x0001;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_READ_NOT_PERMITTED
	public static final int GATT_READ_NOT_PERMIT = 0x0002;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_WRITE_NOT_PERMITTED
	public static final int GATT_WRITE_NOT_PERMIT = 0x0003;
	public static final int GATT_INVALID_PDU = 0x0004;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_INSUFFICIENT_AUTHENTICATION
	public static final int GATT_INSUF_AUTHENTICATION = 0x0005;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_REQUEST_NOT_SUPPORTED
	public static final int GATT_REQ_NOT_SUPPORTED = 0x0006;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_INVALID_OFFSET
	public static final int GATT_INVALID_OFFSET = 0x0007;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_INSUFFICIENT_AUTHORIZATION
	public static final int GATT_INSUF_AUTHORIZATION = 0x0008;
	public static final int GATT_PREPARE_Q_FULL = 0x0009;
	public static final int GATT_NOT_FOUND = 0x000a;
	public static final int GATT_NOT_LONG = 0x000b;
	public static final int GATT_INSUF_KEY_SIZE = 0x000c;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_INVALID_ATTRIBUTE_LENGTH
	public static final int GATT_INVALID_ATTR_LEN = 0x000d;
	public static final int GATT_ERR_UNLIKELY = 0x000e;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_INSUFFICIENT_ENCRYPTION
	public static final int GATT_INSUF_ENCRYPTION = 0x000f;
	public static final int GATT_UNSUPPORT_GRP_TYPE = 0x0010;
	public static final int GATT_INSUF_RESOURCE = 0x0011;
	public static final int GATT_CONTROLLER_BUSY = 0x003A;
	public static final int GATT_UNACCEPT_CONN_INTERVAL = 0x003B;
	public static final int GATT_ILLEGAL_PARAMETER = 0x0087;
	public static final int GATT_NO_RESOURCES = 0x0080;
	public static final int GATT_INTERNAL_ERROR = 0x0081;
	public static final int GATT_WRONG_STATE = 0x0082;
	public static final int GATT_DB_FULL = 0x0083;
	public static final int GATT_BUSY = 0x0084;
	public static final int GATT_CMD_STARTED = 0x0086;
	public static final int GATT_PENDING = 0x0088;
	public static final int GATT_AUTH_FAIL = 0x0089;
	public static final int GATT_MORE = 0x008a;
	public static final int GATT_INVALID_CFG = 0x008b;
	public static final int GATT_SERVICE_STARTED = 0x008c;
	public static final int GATT_ENCRYPTED_NO_MITM = 0x008d;
	public static final int GATT_NOT_ENCRYPTED = 0x008e;
	// https://developer.android.com/reference/android/bluetooth/BluetoothGatt#GATT_CONNECTION_CONGESTED
	public static final int GATT_CONGESTED = 0x008f;
	public static final int GATT_CCCD_CFG_ERROR = 0x00FD;
	public static final int GATT_PROCEDURE_IN_PROGRESS = 0x00FE;
	public static final int GATT_VALUE_OUT_OF_RANGE = 0x00FF;
	public static final int TOO_MANY_OPEN_CONNECTIONS = 0x0101;

	/**
	 * Converts the connection status given by the
	 * {@link android.bluetooth.BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)}
	 * to error name.
	 *
	 * @param error the status number.
	 * @return The error name as stated in the links in {@link GattError} documentation.
	 */
	public static String parseConnectionError(final int error) {
		return switch (error) {
			case GATT_SUCCESS -> "SUCCESS";
			case GATT_CONN_L2C_FAILURE -> "GATT CONN L2C FAILURE";
			case GATT_CONN_TIMEOUT -> "GATT CONN TIMEOUT";
			case GATT_CONN_TERMINATE_PEER_USER -> "GATT CONN TERMINATE PEER USER";
			case GATT_CONN_TERMINATE_LOCAL_HOST -> "GATT CONN TERMINATE LOCAL HOST";
			case GATT_CONN_FAIL_ESTABLISH -> "GATT CONN FAIL ESTABLISH";
			case GATT_CONN_LMP_TIMEOUT -> "GATT CONN LMP TIMEOUT";
			case GATT_CONN_CANCEL -> "GATT CONN CANCEL ";
			case GATT_ERROR -> "GATT ERROR"; // Device not reachable
			default -> "UNKNOWN (" + error + ")";
		};
	}

	/**
	 * Converts the Bluetooth communication status given by other BluetoothGattCallbacks to error
	 * name. It also parses the DFU errors.
	 *
	 * @param error the status number.
	 * @return The error name as stated in the links in {@link GattError} documentation.
	 */
	public static String parse(final int error) {
		return switch (error) {
			case GATT_INVALID_HANDLE -> "GATT INVALID HANDLE";
			case GATT_READ_NOT_PERMIT -> "GATT READ NOT PERMIT";
			case GATT_WRITE_NOT_PERMIT -> "GATT WRITE NOT PERMIT";
			case GATT_INVALID_PDU -> "GATT INVALID PDU";
			case GATT_INSUF_AUTHENTICATION -> "GATT INSUF AUTHENTICATION";
			case GATT_REQ_NOT_SUPPORTED -> "GATT REQ NOT SUPPORTED";
			case GATT_INVALID_OFFSET -> "GATT INVALID OFFSET";
			case GATT_INSUF_AUTHORIZATION -> "GATT INSUF AUTHORIZATION";
			case GATT_PREPARE_Q_FULL -> "GATT PREPARE Q FULL";
			case GATT_NOT_FOUND -> "GATT NOT FOUND";
			case GATT_NOT_LONG -> "GATT NOT LONG";
			case GATT_INSUF_KEY_SIZE -> "GATT INSUF KEY SIZE";
			case GATT_INVALID_ATTR_LEN -> "GATT INVALID ATTR LEN";
			case GATT_ERR_UNLIKELY -> "GATT ERR UNLIKELY";
			case GATT_INSUF_ENCRYPTION -> "GATT INSUF ENCRYPTION";
			case GATT_UNSUPPORT_GRP_TYPE -> "GATT UNSUPPORT GRP TYPE";
			case GATT_INSUF_RESOURCE -> "GATT INSUF RESOURCE";
			case GATT_CONN_LMP_TIMEOUT -> "GATT CONN LMP TIMEOUT";
			case GATT_CONTROLLER_BUSY -> "GATT CONTROLLER BUSY";
			case GATT_UNACCEPT_CONN_INTERVAL -> "GATT UNACCEPT CONN INTERVAL";
			case GATT_ILLEGAL_PARAMETER -> "GATT ILLEGAL PARAMETER";
			case GATT_NO_RESOURCES -> "GATT NO RESOURCES";
			case GATT_INTERNAL_ERROR -> "GATT INTERNAL ERROR";
			case GATT_WRONG_STATE -> "GATT WRONG STATE";
			case GATT_DB_FULL -> "GATT DB FULL";
			case GATT_BUSY -> "GATT BUSY";
			case GATT_ERROR -> "GATT ERROR";
			case GATT_CMD_STARTED -> "GATT CMD STARTED";
			case GATT_PENDING -> "GATT PENDING";
			case GATT_AUTH_FAIL -> "GATT AUTH FAIL";
			case GATT_MORE -> "GATT MORE";
			case GATT_INVALID_CFG -> "GATT INVALID CFG";
			case GATT_SERVICE_STARTED -> "GATT SERVICE STARTED";
			case GATT_ENCRYPTED_NO_MITM -> "GATT ENCRYPTED NO MITM";
			case GATT_NOT_ENCRYPTED -> "GATT NOT ENCRYPTED";
			case GATT_CONGESTED -> "GATT CONGESTED";
			case GATT_CCCD_CFG_ERROR -> "GATT CCCD CFG ERROR";
			case GATT_PROCEDURE_IN_PROGRESS -> "GATT PROCEDURE IN PROGRESS";
			case GATT_VALUE_OUT_OF_RANGE -> "GATT VALUE OUT OF RANGE";
			case TOO_MANY_OPEN_CONNECTIONS -> "TOO MANY OPEN CONNECTIONS";
			default -> "UNKNOWN (" + error + ")";
		};
	}
}
