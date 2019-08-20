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

import no.nordicsemi.android.ble.annotation.PhyValue
import no.nordicsemi.android.ble.callback.PhyCallback

class PhyResult// Parcelable
protected constructor(`in`: Parcel) : PhyCallback, Parcelable {
    var bluetoothDevice: BluetoothDevice? = null
        private set
    @PhyValue
    @get:PhyValue
    var txPhy: Int = 0
        private set
    @PhyValue
    @get:PhyValue
    var rxPhy: Int = 0
        private set

    init {
        bluetoothDevice = `in`.readParcelable(BluetoothDevice::class.java.classLoader)
        txPhy = `in`.readInt()
        rxPhy = `in`.readInt()
    }

    override fun onPhyChanged(
        device: BluetoothDevice,
        @PhyValue txPhy: Int, @PhyValue rxPhy: Int
    ) {
        this.bluetoothDevice = device
        this.txPhy = txPhy
        this.rxPhy = rxPhy
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(bluetoothDevice, flags)
        dest.writeInt(txPhy)
        dest.writeInt(rxPhy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PhyResult> = object : Parcelable.Creator<PhyResult> {
            override fun createFromParcel(`in`: Parcel): PhyResult {
                return PhyResult(`in`)
            }

            override fun newArray(size: Int): Array<PhyResult?> {
                return arrayOfNulls(size)
            }
        }
    }
}
