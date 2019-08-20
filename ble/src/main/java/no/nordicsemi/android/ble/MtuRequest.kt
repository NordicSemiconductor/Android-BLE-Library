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

package no.nordicsemi.android.ble

import android.bluetooth.BluetoothDevice
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.callback.*

class MtuRequest internal constructor(type: Request.Type, @IntRange(from = 23, to = 517) mtu: Int) :
    SimpleValueRequest<MtuCallback>(type), Operation {
    internal val requiredMtu: Int

    init {
        var mtu = mtu
        if (mtu < 23)
            mtu = 23
        if (mtu > 517)
            mtu = 517
        this.requiredMtu = mtu
    }

    override fun setManager(manager: BleManager<*>): MtuRequest {
        super.setManager(manager)
        return this
    }

    override fun done(callback: SuccessCallback): MtuRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): MtuRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): MtuRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): MtuRequest {
        super.before(callback)
        return this
    }

    override fun with(callback: MtuCallback): MtuRequest {
        super.with(callback)
        return this
    }

    internal fun notifyMtuChanged(
        device: BluetoothDevice,
        @IntRange(from = 23, to = 517) mtu: Int
    ) {
        valueCallback?.onMtuChanged(device, mtu)
    }
}
