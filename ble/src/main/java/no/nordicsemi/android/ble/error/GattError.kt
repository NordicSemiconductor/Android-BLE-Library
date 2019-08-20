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
package no.nordicsemi.android.ble.error

import android.bluetooth.BluetoothGatt

/**
 * Parses the GATT and HCI errors to human readable strings.
 *
 *
 * See: [gatt_api.h](https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/master/stack/include/gatt_api.h) for details.<br></br>
 * See also: [hcidefs.h](https://android.googlesource.com/platform/external/libnfc-nci/+/master/src/include/hcidefs.h#447) for other possible HCI errors.
 */
object GattError {
    const val GATT_SUCCESS = BluetoothGatt.GATT_SUCCESS
    const val GATT_CONN_L2C_FAILURE = 0x01
    const val GATT_CONN_TIMEOUT = 0x08
    const val GATT_CONN_TERMINATE_PEER_USER = 0x13
    const val GATT_CONN_TERMINATE_LOCAL_HOST = 0x16
    const val GATT_CONN_FAIL_ESTABLISH = 0x3E
    const val GATT_CONN_LMP_TIMEOUT = 0x22
    const val GATT_CONN_CANCEL = 0x0100
    const val GATT_ERROR = 0x0085 // Device not reachable

    const val GATT_INVALID_HANDLE = 0x0001
    const val GATT_READ_NOT_PERMIT = 0x0002
    const val GATT_WRITE_NOT_PERMIT = 0x0003
    const val GATT_INVALID_PDU = 0x0004
    const val GATT_INSUF_AUTHENTICATION = 0x0005
    const val GATT_REQ_NOT_SUPPORTED = 0x0006
    const val GATT_INVALID_OFFSET = 0x0007
    const val GATT_INSUF_AUTHORIZATION = 0x0008
    const val GATT_PREPARE_Q_FULL = 0x0009
    const val GATT_NOT_FOUND = 0x000a
    const val GATT_NOT_LONG = 0x000b
    const val GATT_INSUF_KEY_SIZE = 0x000c
    const val GATT_INVALID_ATTR_LEN = 0x000d
    const val GATT_ERR_UNLIKELY = 0x000e
    const val GATT_INSUF_ENCRYPTION = 0x000f
    const val GATT_UNSUPPORT_GRP_TYPE = 0x0010
    const val GATT_INSUF_RESOURCE = 0x0011
    const val GATT_CONTROLLER_BUSY = 0x003A
    const val GATT_UNACCEPT_CONN_INTERVAL = 0x003B
    const val GATT_ILLEGAL_PARAMETER = 0x0087
    const val GATT_NO_RESOURCES = 0x0080
    const val GATT_INTERNAL_ERROR = 0x0081
    const val GATT_WRONG_STATE = 0x0082
    const val GATT_DB_FULL = 0x0083
    const val GATT_BUSY = 0x0084
    const val GATT_CMD_STARTED = 0x0086
    const val GATT_PENDING = 0x0088
    const val GATT_AUTH_FAIL = 0x0089
    const val GATT_MORE = 0x008a
    const val GATT_INVALID_CFG = 0x008b
    const val GATT_SERVICE_STARTED = 0x008c
    const val GATT_ENCRYPTED_NO_MITM = 0x008d
    const val GATT_NOT_ENCRYPTED = 0x008e
    const val GATT_CONGESTED = 0x008f
    const val GATT_CCCD_CFG_ERROR = 0x00FD
    const val GATT_PROCEDURE_IN_PROGRESS = 0x00FE
    const val GATT_VALUE_OUT_OF_RANGE = 0x00FF
    const val TOO_MANY_OPEN_CONNECTIONS = 0x0101

    /**
     * Converts the connection status given by the
     * [android.bluetooth.BluetoothGattCallback.onConnectionStateChange]
     * to error name.
     *
     * @param error the status number.
     * @return The error name as stated in the links in [GattError] documentation.
     */
    fun parseConnectionError(error: Int): String {
        when (error) {
            GATT_SUCCESS -> return "SUCCESS"
            GATT_CONN_L2C_FAILURE -> return "GATT CONN L2C FAILURE"
            GATT_CONN_TIMEOUT -> return "GATT CONN TIMEOUT"
            GATT_CONN_TERMINATE_PEER_USER -> return "GATT CONN TERMINATE PEER USER"
            GATT_CONN_TERMINATE_LOCAL_HOST -> return "GATT CONN TERMINATE LOCAL HOST"
            GATT_CONN_FAIL_ESTABLISH -> return "GATT CONN FAIL ESTABLISH"
            GATT_CONN_LMP_TIMEOUT -> return "GATT CONN LMP TIMEOUT"
            GATT_CONN_CANCEL -> return "GATT CONN CANCEL "
            GATT_ERROR -> return "GATT ERROR" // Device not reachable
            else -> return "UNKNOWN ($error)"
        }
    }

    /**
     * Converts the Bluetooth communication status given by other BluetoothGattCallbacks to error
     * name. It also parses the DFU errors.
     *
     * @param error the status number.
     * @return The error name as stated in the links in [GattError] documentation.
     */
    fun parse(error: Int): String {
        when (error) {
            GATT_INVALID_HANDLE -> return "GATT INVALID HANDLE"
            GATT_READ_NOT_PERMIT -> return "GATT READ NOT PERMIT"
            GATT_WRITE_NOT_PERMIT -> return "GATT WRITE NOT PERMIT"
            GATT_INVALID_PDU -> return "GATT INVALID PDU"
            GATT_INSUF_AUTHENTICATION -> return "GATT INSUF AUTHENTICATION"
            GATT_REQ_NOT_SUPPORTED -> return "GATT REQ NOT SUPPORTED"
            GATT_INVALID_OFFSET -> return "GATT INVALID OFFSET"
            GATT_INSUF_AUTHORIZATION -> return "GATT INSUF AUTHORIZATION"
            GATT_PREPARE_Q_FULL -> return "GATT PREPARE Q FULL"
            GATT_NOT_FOUND -> return "GATT NOT FOUND"
            GATT_NOT_LONG -> return "GATT NOT LONG"
            GATT_INSUF_KEY_SIZE -> return "GATT INSUF KEY SIZE"
            GATT_INVALID_ATTR_LEN -> return "GATT INVALID ATTR LEN"
            GATT_ERR_UNLIKELY -> return "GATT ERR UNLIKELY"
            GATT_INSUF_ENCRYPTION -> return "GATT INSUF ENCRYPTION"
            GATT_UNSUPPORT_GRP_TYPE -> return "GATT UNSUPPORT GRP TYPE"
            GATT_INSUF_RESOURCE -> return "GATT INSUF RESOURCE"
            GATT_CONN_LMP_TIMEOUT -> return "GATT CONN LMP TIMEOUT"
            GATT_CONTROLLER_BUSY -> return "GATT CONTROLLER BUSY"
            GATT_UNACCEPT_CONN_INTERVAL -> return "GATT UNACCEPT CONN INTERVAL"
            GATT_ILLEGAL_PARAMETER -> return "GATT ILLEGAL PARAMETER"
            GATT_NO_RESOURCES -> return "GATT NO RESOURCES"
            GATT_INTERNAL_ERROR -> return "GATT INTERNAL ERROR"
            GATT_WRONG_STATE -> return "GATT WRONG STATE"
            GATT_DB_FULL -> return "GATT DB FULL"
            GATT_BUSY -> return "GATT BUSY"
            GATT_ERROR -> return "GATT ERROR"
            GATT_CMD_STARTED -> return "GATT CMD STARTED"
            GATT_PENDING -> return "GATT PENDING"
            GATT_AUTH_FAIL -> return "GATT AUTH FAIL"
            GATT_MORE -> return "GATT MORE"
            GATT_INVALID_CFG -> return "GATT INVALID CFG"
            GATT_SERVICE_STARTED -> return "GATT SERVICE STARTED"
            GATT_ENCRYPTED_NO_MITM -> return "GATT ENCRYPTED NO MITM"
            GATT_NOT_ENCRYPTED -> return "GATT NOT ENCRYPTED"
            GATT_CONGESTED -> return "GATT CONGESTED"
            GATT_CCCD_CFG_ERROR -> return "GATT CCCD CFG ERROR"
            GATT_PROCEDURE_IN_PROGRESS -> return "GATT PROCEDURE IN PROGRESS"
            GATT_VALUE_OUT_OF_RANGE -> return "GATT VALUE OUT OF RANGE"
            TOO_MANY_OPEN_CONNECTIONS -> return "TOO MANY OPEN CONNECTIONS"
            else -> return "UNKNOWN ($error)"
        }
    }
}
