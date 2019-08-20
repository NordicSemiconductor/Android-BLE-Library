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
import no.nordicsemi.android.ble.annotation.PhyMask
import no.nordicsemi.android.ble.annotation.PhyOption
import no.nordicsemi.android.ble.annotation.PhyValue
import no.nordicsemi.android.ble.callback.*

class PhyRequest : SimpleValueRequest<PhyCallback>, Operation {

    @get:PhyMask
    internal val preferredTxPhy: Int
    @get:PhyMask
    internal val preferredRxPhy: Int
    @get:PhyOption
    internal val preferredPhyOptions: Int

    internal constructor(type: Request.Type) : super(type) {
        this.preferredTxPhy = 0
        this.preferredRxPhy = 0
        this.preferredPhyOptions = 0
    }

    internal constructor(
        type: Request.Type,
        @PhyMask txPhy: Int, @PhyMask rxPhy: Int, @PhyOption phyOptions: Int
    ) : super(type) {
        var txPhy = txPhy
        var rxPhy = rxPhy
        var phyOptions = phyOptions
        if (txPhy and (PHY_LE_1M_MASK or PHY_LE_2M_MASK or PHY_LE_CODED_MASK).inv() > 0)
            txPhy = PHY_LE_1M_MASK
        if (rxPhy and (PHY_LE_1M_MASK or PHY_LE_2M_MASK or PHY_LE_CODED_MASK).inv() > 0)
            rxPhy = PHY_LE_1M_MASK
        if (phyOptions < PHY_OPTION_NO_PREFERRED || phyOptions > PHY_OPTION_S8)
            phyOptions = PHY_OPTION_NO_PREFERRED
        this.preferredTxPhy = txPhy
        this.preferredRxPhy = rxPhy
        this.preferredPhyOptions = phyOptions
    }

    override fun setManager(manager: BleManager<*>): PhyRequest {
        super.setManager(manager)
        return this
    }

    override fun done(callback: SuccessCallback): PhyRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): PhyRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): PhyRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): PhyRequest {
        super.before(callback)
        return this
    }

    override fun with(callback: PhyCallback): PhyRequest {
        super.with(callback)
        return this
    }

    internal fun notifyPhyChanged(
        device: BluetoothDevice,
        @PhyValue txPhy: Int, @PhyValue rxPhy: Int
    ) {
        valueCallback?.onPhyChanged(device, txPhy, rxPhy)
    }

    internal fun notifyLegacyPhy(device: BluetoothDevice) {
        valueCallback?.onPhyChanged(device, PhyCallback.PHY_LE_1M, PhyCallback.PHY_LE_1M)
    }

    companion object {

        /**
         * Bluetooth LE 1M PHY mask. Used to specify LE 1M Physical Channel as one of many available
         * options in a bitmask.
         */
        const val PHY_LE_1M_MASK = 1

        /**
         * Bluetooth LE 2M PHY mask. Used to specify LE 2M Physical Channel as one of many available
         * options in a bitmask.
         */
        const val PHY_LE_2M_MASK = 2

        /**
         * Bluetooth LE Coded PHY mask. Used to specify LE Coded Physical Channel as one of many
         * available options in a bitmask.
         */
        const val PHY_LE_CODED_MASK = 4

        /**
         * No preferred coding when transmitting on the LE Coded PHY.
         */
        const val PHY_OPTION_NO_PREFERRED = 0

        /**
         * Prefer the S=2 coding to be used when transmitting on the LE Coded PHY.
         */
        const val PHY_OPTION_S2 = 1

        /**
         * Prefer the S=8 coding to be used when transmitting on the LE Coded PHY.
         */
        const val PHY_OPTION_S8 = 2
    }
}
