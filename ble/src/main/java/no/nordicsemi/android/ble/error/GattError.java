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
 * Parses the error numbers according to the <b>gatt_api.h</b> file from bluedroid stack.
 * See: https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/android-5.1.0_r1/stack/include/gatt_api.h (and other versions) for details.
 * See also: https://android.googlesource.com/platform/external/libnfc-nci/+/master/src/include/hcidefs.h#447 for other possible HCI errors.
 */
public class GattError {
	/**
	 * Converts the connection status given by the {@link android.bluetooth.BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)} to error name.
	 * @param error the status number
	 * @return the error name as stated in the gatt_api.h file
	 */
	public static String parseConnectionError(final int error) {
		switch (error) {
			case BluetoothGatt.GATT_SUCCESS:
				return "SUCCESS";
			case 0x01:
				return "GATT CONN L2C FAILURE";
			case 0x08:
				return "GATT CONN TIMEOUT";
			case 0x13:
				return "GATT CONN TERMINATE PEER USER";
			case 0x16:
				return "GATT CONN TERMINATE LOCAL HOST";
			case 0x3E:
				return "GATT CONN FAIL ESTABLISH";
			case 0x22:
				return "GATT CONN LMP TIMEOUT";
			case 0x0100:
				return "GATT CONN CANCEL ";
			case 0x0085:
				return "GATT ERROR"; // Device not reachable
			default:
				return "UNKNOWN (" + error + ")";
		}
	}

	/**
	 * Converts the bluetooth communication status given by other BluetoothGattCallbacks to error name. It also parses the DFU errors.
	 * @param error the status number
	 * @return the error name as stated in the gatt_api.h file
	 */
	public static String parse(final int error) {
		switch (error) {
			case 0x0001:
				return "GATT INVALID HANDLE";
			case 0x0002:
				return "GATT READ NOT PERMIT";
			case 0x0003:
				return "GATT WRITE NOT PERMIT";
			case 0x0004:
				return "GATT INVALID PDU";
			case 0x0005:
				return "GATT INSUF AUTHENTICATION";
			case 0x0006:
				return "GATT REQ NOT SUPPORTED";
			case 0x0007:
				return "GATT INVALID OFFSET";
			case 0x0008:
				return "GATT INSUF AUTHORIZATION";
			case 0x0009:
				return "GATT PREPARE Q FULL";
			case 0x000a:
				return "GATT NOT FOUND";
			case 0x000b:
				return "GATT NOT LONG";
			case 0x000c:
				return "GATT INSUF KEY SIZE";
			case 0x000d:
				return "GATT INVALID ATTR LEN";
			case 0x000e:
				return "GATT ERR UNLIKELY";
			case 0x000f:
				return "GATT INSUF ENCRYPTION";
			case 0x0010:
				return "GATT UNSUPPORT GRP TYPE";
			case 0x0011:
				return "GATT INSUF RESOURCE";
			case 0x0022:
				return "GATT CONN LMP TIMEOUT";
			case 0x003A:
				return "GATT CONTROLLER BUSY";
			case 0x003B:
				return "GATT UNACCEPT CONN INTERVAL";
			case 0x0087:
				return "GATT ILLEGAL PARAMETER";
			case 0x0080:
				return "GATT NO RESOURCES";
			case 0x0081:
				return "GATT INTERNAL ERROR";
			case 0x0082:
				return "GATT WRONG STATE";
			case 0x0083:
				return "GATT DB FULL";
			case 0x0084:
				return "GATT BUSY";
			case 0x0085:
				return "GATT ERROR";
			case 0x0086:
				return "GATT CMD STARTED";
			case 0x0088:
				return "GATT PENDING";
			case 0x0089:
				return "GATT AUTH FAIL";
			case 0x008a:
				return "GATT MORE";
			case 0x008b:
				return "GATT INVALID CFG";
			case 0x008c:
				return "GATT SERVICE STARTED";
			case 0x008d:
				return "GATT ENCRYPTED NO MITM";
			case 0x008e:
				return "GATT NOT ENCRYPTED";
			case 0x008f:
				return "GATT CONGESTED";
			case 0x00FD:
				return "GATT CCCD CFG ERROR";
			case 0x00FE:
				return "GATT PROCEDURE IN PROGRESS";
			case 0x00FF:
				return "GATT VALUE OUT OF RANGE";
			case 0x0101:
				return "TOO MANY OPEN CONNECTIONS";
			default:
				return "UNKNOWN (" + error + ")";
		}
	}
}
