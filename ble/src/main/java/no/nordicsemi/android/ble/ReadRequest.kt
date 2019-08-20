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
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import no.nordicsemi.android.ble.callback.*
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.data.DataFilter
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataStream
import no.nordicsemi.android.ble.exception.*

class ReadRequest : SimpleValueRequest<DataReceivedCallback>, Operation {
    private var progressCallback: ReadProgressCallback? = null
    private var dataMerger: DataMerger? = null
    private var buffer: DataStream? = null
    private var filter: DataFilter? = null
    private var count = 0

    internal constructor(type: Request.Type) : super(type)

    internal constructor(type: Request.Type, characteristic: BluetoothGattCharacteristic?) : super(
        type,
        characteristic
    )

    internal constructor(type: Request.Type, descriptor: BluetoothGattDescriptor?) : super(
        type,
        descriptor
    )

    override fun setManager(manager: BleManager<*>): ReadRequest {
        super.setManager(manager)
        return this
    }

    override fun done(callback: SuccessCallback): ReadRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): ReadRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): ReadRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): ReadRequest {
        super.before(callback)
        return this
    }

    override fun with(callback: DataReceivedCallback): ReadRequest {
        super.with(callback)
        return this
    }

    /**
     * Sets a filter which allows to skip some incoming data.
     *
     * @param filter the data filter.
     * @return The request.
     */
    fun filter(filter: DataFilter): ReadRequest {
        this.filter = filter
        return this
    }

    /**
     * Adds a merger that will be used to merge multiple packets into a single Data.
     * The merger may modify each packet if necessary.
     *
     * @return The request.
     */
    fun merge(merger: DataMerger): ReadRequest {
        this.dataMerger = merger
        this.progressCallback = null
        return this
    }

    /**
     * Adds a merger that will be used to merge multiple packets into a single Data.
     * The merger may modify each packet if necessary.
     *
     * @return The request.
     */
    fun merge(
        merger: DataMerger,
        callback: ReadProgressCallback
    ): ReadRequest {
        this.dataMerger = merger
        this.progressCallback = callback
        return this
    }

    /**
     * Same as [.await], but if the response class extends
     * [ProfileReadResponse] and the received response is not valid
     * ([ProfileReadResponse.isValid] returns false), this method will
     * throw an exception.
     *
     * @param responseClass the response class. This class will be instantiate, therefore it
     * has to have a default constructor.
     * @return The object with the response.
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [BluetoothGatt.GATT_SUCCESS].
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     * [ProfileReadResponse.onDataReceived]
     * failed to parse the data correctly and called
     * [ProfileReadResponse.onInvalidDataReceived]).
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     */
    @Throws(
        RequestFailedException::class,
        InvalidDataException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun <E : ProfileReadResponse> awaitValid(responseClass: Class<E>): E {
        val response = await(responseClass)
        if (!response.isValid) {
            throw InvalidDataException(response)
        }
        return response
    }

    /**
     * Same as [.await], but if the response class extends
     * [ProfileReadResponse] and the received response is not valid
     * ([ProfileReadResponse.isValid] returns false), this method will
     * throw an exception.
     *
     * @param response the response object.
     * @return The object with the response.
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [BluetoothGatt.GATT_SUCCESS].
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     * [ProfileReadResponse.onDataReceived]
     * failed to parse the data correctly and called
     * [ProfileReadResponse.onInvalidDataReceived]).
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     */
    @Throws(
        RequestFailedException::class,
        InvalidDataException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun <E : ProfileReadResponse> awaitValid(response: E): E {
        await(response)
        if (!response.isValid) {
            throw InvalidDataException(response)
        }
        return response
    }

    internal fun matches(packet: ByteArray): Boolean {
        return filter == null || filter!!.filter(packet)
    }

    internal fun notifyValueChanged(device: BluetoothDevice, value: ByteArray?) {
        // Keep a reference to the value callback, as it may change during execution
        val valueCallback = this.valueCallback

        // With no value callback there is no need for any merging

        if (dataMerger == null) {
            valueCallback?.onDataReceived(device, Data(value))
        } else {
            if (progressCallback != null)
                progressCallback!!.onPacketReceived(device, value, count)
            if (buffer == null)
                buffer = DataStream()
            if (dataMerger!!.merge(buffer!!, value, count++)) {
                valueCallback?.onDataReceived(device, buffer!!.toData())
                buffer = null
                count = 0
            } // else
            // wait for more packets to be merged
        }
    }

    internal fun hasMore(): Boolean {
        return count > 0
    }
}
