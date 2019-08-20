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

package no.nordicsemi.android.ble.response

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

import no.nordicsemi.android.ble.callback.DataReceivedCallback
import no.nordicsemi.android.ble.data.Data

/**
 * Generic read response class that returns the data received and the device from which data
 * were read.
 * Overriding class must call super on [.onDataReceived] in
 * order to make getters work properly.
 */
open class ReadResponse : DataReceivedCallback, Parcelable {
    var bluetoothDevice: BluetoothDevice? = null
        private set
    var rawData: Data? = null
        private set

    constructor() {
        // empty
    }

    // Parcelable
    protected constructor(`in`: Parcel) {
        bluetoothDevice = `in`.readParcelable(BluetoothDevice::class.java.classLoader)
        rawData = `in`.readParcelable(Data::class.java.classLoader)
    }

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        this.bluetoothDevice = device
        this.rawData = data
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(bluetoothDevice, flags)
        dest.writeParcelable(rawData, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ReadResponse> = object : Parcelable.Creator<ReadResponse> {
            override fun createFromParcel(`in`: Parcel): ReadResponse {
                return ReadResponse(`in`)
            }

            override fun newArray(size: Int): Array<ReadResponse?> {
                return arrayOfNulls(size)
            }
        }
    }
}
