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

import androidx.annotation.IntRange

import no.nordicsemi.android.ble.callback.RssiCallback

class RssiResult// Parcelable
protected constructor(`in`: Parcel) : RssiCallback, Parcelable {
    var bluetoothDevice: BluetoothDevice? = null
        private set
    @IntRange(from = -128, to = 20)
    @get:IntRange(from = -128, to = 20)
    var rssi: Int = 0
        private set

    init {
        bluetoothDevice = `in`.readParcelable(BluetoothDevice::class.java.classLoader)
        rssi = `in`.readInt()
    }

    override fun onRssiRead(
        device: BluetoothDevice,
        @IntRange(from = -128, to = 20) rssi: Int
    ) {
        this.bluetoothDevice = device
        this.rssi = rssi
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(bluetoothDevice, flags)
        dest.writeInt(rssi)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RssiResult> = object : Parcelable.Creator<RssiResult> {
            override fun createFromParcel(`in`: Parcel): RssiResult {
                return RssiResult(`in`)
            }

            override fun newArray(size: Int): Array<RssiResult?> {
                return arrayOfNulls(size)
            }
        }
    }
}
