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
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.annotation.WriteType
import no.nordicsemi.android.ble.callback.*
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.data.DataSplitter
import no.nordicsemi.android.ble.data.DefaultMtuSplitter
import java.util.*

class WriteRequest : SimpleValueRequest<DataSentCallback>, Operation {
    private val data: ByteArray?
    /**
     * Returns the write type that should be used to send the data.
     *
     * @return The write type.
     */
    @get:WriteType
    internal val writeType: Int
    private var progressCallback: WriteProgressCallback? = null
    private var dataSplitter: DataSplitter? = null
    private var currentChunk: ByteArray? = null
    private var nextChunk: ByteArray? = null
    private var count = 0
    private var complete = false

    @JvmOverloads
    internal constructor(
        type: Request.Type,
        characteristic: BluetoothGattCharacteristic? = null
    ) : super(type, characteristic) {
        // not used:
        this.data = null
        this.writeType = 0
        // getData(int) isn't called on enabling and disabling notifications/indications.
        this.complete = true
    }

    internal constructor(
        type: Request.Type, characteristic: BluetoothGattCharacteristic?,
        data: ByteArray?,
        @IntRange(from = 0) offset: Int, @IntRange(from = 0) length: Int,
        @WriteType writeType: Int
    ) : super(type, characteristic) {
        this.data = copy(data, offset, length)
        this.writeType = writeType
    }

    internal constructor(
        type: Request.Type, descriptor: BluetoothGattDescriptor?,
        data: ByteArray?,
        @IntRange(from = 0) offset: Int, @IntRange(from = 0) length: Int
    ) : super(type, descriptor) {
        this.data = copy(data, offset, length)
        this.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    }

    override fun setManager(manager: BleManager<*>): WriteRequest {
        super.setManager(manager)
        return this
    }

    override fun done(callback: SuccessCallback): WriteRequest {
        super.done(callback)
        return this
    }

    override fun fail(callback: FailCallback): WriteRequest {
        super.fail(callback)
        return this
    }

    override fun invalid(callback: InvalidRequestCallback): WriteRequest {
        super.invalid(callback)
        return this
    }

    override fun before(callback: BeforeCallback): WriteRequest {
        super.before(callback)
        return this
    }

    override fun with(callback: DataSentCallback): WriteRequest {
        super.with(callback)
        return this
    }

    /**
     * Adds a splitter that will be used to cut given data into multiple packets.
     * The splitter may modify each packet if necessary, i.e. add a flag indicating first packet,
     * continuation or the last packet.
     *
     * @param splitter an implementation of a splitter.
     * @return The request.
     * @see .split
     */
    fun split(splitter: DataSplitter): WriteRequest {
        this.dataSplitter = splitter
        this.progressCallback = null
        return this
    }

    /**
     * Adds a splitter that will be used to cut given data into multiple packets.
     * The splitter may modify each packet if necessary, i.e. add a flag indicating first packet,
     * continuation or the last packet.
     *
     * @param splitter an implementation of a splitter.
     * @param callback the progress callback that will be notified each time a packet was sent.
     * @return The request.
     * @see .split
     */
    fun split(
        splitter: DataSplitter,
        callback: WriteProgressCallback
    ): WriteRequest {
        this.dataSplitter = splitter
        this.progressCallback = callback
        return this
    }

    /**
     * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
     * bytes long packets.
     *
     * @return The request.
     */
    fun split(): WriteRequest {
        this.dataSplitter = MTU_SPLITTER
        this.progressCallback = null
        return this
    }

    /**
     * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
     * bytes long packets.
     *
     * @param callback the progress callback that will be notified each time a packet was sent.
     * @return The request.
     */
    fun split(callback: WriteProgressCallback): WriteRequest {
        this.dataSplitter = MTU_SPLITTER
        this.progressCallback = callback
        return this
    }

    /**
     * This method makes sure the data sent will be split to at-most MTU-3 bytes long packets.
     * This is because Long Write does not work with Reliable Write.
     */
    internal fun forceSplit() {
        if (dataSplitter == null)
            split()
    }

    /**
     * Returns the next chunk to be sent. If data splitter was not set the date returned may
     * be longer than MTU. Android will try to send them using Long Write sub-procedure if
     * write type is [BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT]. Other write types
     * will cause the data to be truncated.
     *
     * @param mtu the current MTU.
     * @return The next bytes to be sent.
     */
    internal fun getData(@IntRange(from = 23, to = 517) mtu: Int): ByteArray {
        if (dataSplitter == null || data == null) {
            complete = true
            currentChunk = data
            return currentChunk!!
        }

        // Write Request and Write Command require 3 bytes for handler and op code.
        // Write Signed requires 12 bytes, as the signature is sent.
        val maxLength = if (writeType != BluetoothGattCharacteristic.WRITE_TYPE_SIGNED)
            mtu - 3
        else
            mtu - 12

        var chunk = nextChunk
        // Get the first chunk.
        if (chunk == null) {
            chunk = dataSplitter!!.chunk(data, count, maxLength)
        }
        // If there's something to send, check if there are any more packets to be sent later.
        if (chunk != null) {
            nextChunk = dataSplitter!!.chunk(data, count + 1, maxLength)
        }
        // If there's no next packet left, we are done.
        if (nextChunk == null) {
            complete = true
        }
        currentChunk = chunk
        return currentChunk!!
    }

    /**
     * Method called when packet has been sent and confirmed (when Write With Response was used),
     * or added to local outgoing buffer (when Write Without Response was used).
     *
     * @param device the target device.
     * @param data   the data received in the
     * [android.bluetooth.BluetoothGattCallback.onCharacteristicWrite].
     * @return True, if the data received are equal to data sent.
     */
    internal fun notifyPacketSent(device: BluetoothDevice, data: ByteArray?): Boolean {
        if (progressCallback != null)
            progressCallback!!.onPacketSent(device, data, count)
        count++
        if (complete && valueCallback != null)
            valueCallback!!.onDataSent(device, Data(this@WriteRequest.data))
        return Arrays.equals(data, currentChunk)
    }

    /**
     * Returns whether there are more bytes to be sent from this Write Request.
     *
     * @return True if not all data were sent, false if the request is complete.
     */
    internal fun hasMore(): Boolean {
        return !complete
    }

    companion object {
        private val MTU_SPLITTER = DefaultMtuSplitter()

        private fun copy(
            value: ByteArray?,
            @IntRange(from = 0) offset: Int,
            @IntRange(from = 0) length: Int
        ): ByteArray? {
            if (value == null || offset > value.size)
                return null
            val maxLength = Math.min(value.size - offset, length)
            val copy = ByteArray(maxLength)
            System.arraycopy(value, offset, copy, 0, maxLength)
            return copy
        }
    }
}
