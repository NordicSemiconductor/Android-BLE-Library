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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.annotation.PhyMask
import no.nordicsemi.android.ble.callback.BeforeCallback
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.callback.InvalidRequestCallback
import no.nordicsemi.android.ble.callback.SuccessCallback

/**
 * The connect request is used to connect to a Bluetooth LE device. The request will end when
 * the device gets connected, the connection timeouts, or an error occurs.
 *
 *
 * The [.done] callback will be called after the device is ready, that is
 * when it is connected, the services were discovered, the required services were found and the
 * initialization queue set in [BleManager.BleManagerGattCallback.initialize] is complete
 * (without or with errors).
 */
class ConnectRequest internal constructor(type: Request.Type, val device: BluetoothDevice) :
    TimeoutableRequest(type) {
    @PhyMask
    @get:PhyMask
    internal var preferredPhy: Int = 0
        private set
    @IntRange(from = 0)
    private var attempt = 0
    @IntRange(from = 0)
    private var retries = 0
    @IntRange(from = 0)
    @get:IntRange(from = 0)
    internal var retryDelay = 0
        private set
    private var autoConnect = false

    internal val isFirstAttempt: Boolean
        get() = attempt++ == 0

    init {
        this.preferredPhy = PhyRequest.PHY_LE_1M_MASK
    }

    override fun setManager(manager: BleManager<*>): ConnectRequest {
        super.setManager(manager)
        return this
    }

    override fun timeout(@IntRange(from = 0) timeout: Long): ConnectRequest {
        super.timeout(timeout)
        return this
    }

    /**
     * Use to set a completion callback. The callback will be invoked when the operation has
     * finished successfully unless [.await] or its variant was used, in which case this
     * callback will be ignored.
     *
     *
     * The done callback will also be called when one or more of initialization requests has
     * failed due to a reason other than disconnect event. This is because
     * [BleManagerCallbacks.onDeviceReady] is called no matter
     * if the requests succeeded, or not. Set failure callbacks to initialization requests
     * to get information about failures.
     *
     * @param callback the callback.
     * @return The request.
     */
    override fun done(callback: SuccessCallback): ConnectRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): ConnectRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): ConnectRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): ConnectRequest {
        super.before(callback)
        return this
    }

    /**
     * Sets an optional retry count. The BleManager will do that many attempts to connect to the
     * device in case of an error. The library will NOT retry if the device is not reachable,
     * that is when the 30 sec. timeout occurs. In that case the app should scan before
     * connecting, to make sure the target is in range.
     *
     * @param count how many times should the BleManager retry to connect.
     * @return The request.
     * @see .retry
     */
    fun retry(@IntRange(from = 0) count: Int): ConnectRequest {
        this.retries = count
        this.retryDelay = 0
        return this
    }

    /**
     * Sets an optional retry count and a delay that the process will wait before each connection
     * attempt. The library will NOT retry if the device is not reachable, that is when the 30 sec.
     * timeout occurs. In that case the app should scan before connecting, to make sure the
     * target is in range.
     *
     * @param count how many times should the BleManager retry to connect.
     * @param delay the delay between each connection attempt, in milliseconds.
     * The real delay will be 200 ms longer than specified, as
     * [BluetoothGatt.clone] is estimated to last
     * [200 ms][BleManager.internalConnect].
     * @return The request.
     * @see .retry
     */
    fun retry(
        @IntRange(from = 0) count: Int,
        @IntRange(from = 0) delay: Int
    ): ConnectRequest {
        this.retries = count
        this.retryDelay = delay
        return this
    }

    /**
     * This method replaces the [BleManager.shouldAutoConnect] method.
     *
     *
     * Sets whether to connect to the remote device just once (false) or to add the address to
     * white list of devices that will be automatically connect as soon as they become available
     * (true). In the latter case, if Bluetooth adapter is enabled, Android scans periodically
     * for devices from the white list and, if an advertising packet is received from such, it tries
     * to connect to it. When the connection is lost, the system will keep trying to reconnect to
     * it. If method is called with parameter set to true, and the connection to the device is
     * lost, the [BleManagerCallbacks.onLinkLossOccurred] callback is
     * called instead of [BleManagerCallbacks.onDeviceDisconnected].
     *
     *
     * This feature works much better on newer Android phone models and may have issues on older
     * phones.
     *
     *
     * This method should only be used with bonded devices, as otherwise the device may change
     * it's address. It will however work also with non-bonded devices with private static address.
     * A connection attempt to a non-bonded device with private resolvable address will fail.
     *
     *
     * The first connection to a device will always be created with autoConnect flag to false
     * (see [BluetoothDevice.connectGatt]). This is
     * to make it quick as the user most probably waits for a quick response. If autoConnect is
     * used (true), the following connections will be done using [BluetoothGatt.connect],
     * which forces the autoConnect parameter to true.
     *
     * @param autoConnect true to use autoConnect feature on the second and following connections.
     * The first connection is always done with autoConnect parameter equal to
     * false, to make it faster and allow to timeout it the device is unreachable.
     * Default value is false.
     * @return The request.
     */
    fun useAutoConnect(autoConnect: Boolean): ConnectRequest {
        this.autoConnect = autoConnect
        return this
    }

    /**
     * Sets the preferred PHY used for connection. Th value should be a bitmask composed of
     * [PhyRequest.PHY_LE_1M_MASK], [PhyRequest.PHY_LE_2M_MASK] or
     * [PhyRequest.PHY_LE_CODED_MASK].
     *
     *
     * Different PHYs are available only on more recent devices with Android 8+.
     * Check [BluetoothAdapter.isLe2MPhySupported] and
     * [BluetoothAdapter.isLeCodedPhySupported] if required PHYs are supported by this
     * Android device. The default PHY is [PhyRequest.PHY_LE_1M_MASK].
     *
     * @param phy preferred PHY for connections to remote LE device. Bitwise OR of any of
     * [PhyRequest.PHY_LE_1M_MASK], [PhyRequest.PHY_LE_2M_MASK],
     * and [PhyRequest.PHY_LE_CODED_MASK]. This option does not take effect
     * if `autoConnect` is set to true.
     * @return The request.
     */
    fun usePreferredPhy(@PhyMask phy: Int): ConnectRequest {
        this.preferredPhy = phy
        return this
    }

    internal fun canRetry(): Boolean {
        if (retries > 0) {
            retries -= 1
            return true
        }
        return false
    }

    internal fun shouldAutoConnect(): Boolean {
        return autoConnect
    }
}
