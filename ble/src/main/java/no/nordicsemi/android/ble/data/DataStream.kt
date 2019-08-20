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

package no.nordicsemi.android.ble.data

import androidx.annotation.IntRange

import java.io.ByteArrayOutputStream

class DataStream {
    private val buffer: ByteArrayOutputStream

    init {
        buffer = ByteArrayOutputStream()
    }

    fun write(data: ByteArray?): Boolean {
        return if (data == null) false else write(data, 0, data.size)

    }

    fun write(
        data: ByteArray?,
        @IntRange(from = 0) offset: Int, @IntRange(from = 0) length: Int
    ): Boolean {
        if (data == null || data.size < offset)
            return false

        val len = Math.min(data.size - offset, length)
        buffer.write(data, offset, len)
        return true
    }

    fun write(data: Data?): Boolean {
        return data != null && write(data.value)
    }

    @IntRange(from = 0)
    fun size(): Int {
        return buffer.size()
    }

    fun toByteArray(): ByteArray {
        return buffer.toByteArray()
    }

    fun toData(): Data {
        return Data(buffer.toByteArray())
    }
}
