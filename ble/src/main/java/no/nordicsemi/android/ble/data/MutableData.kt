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

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

import androidx.annotation.IntRange

class MutableData : Data {
    // private final static float FLOAT_EPSILON = 1e-128f;

    constructor() : super()

    constructor(data: ByteArray?) : super(data)

    /**
     * Updates the locally stored value of this data.
     *
     * @param value New value
     * @return true if the locally stored value has been set, false if the
     * requested value could not be stored locally.
     */
    fun setValue(value: ByteArray?): Boolean {
        this.value = value
        return true
    }

    /**
     * Updates the byte at offset position.
     *
     * @param value  Byte to set
     * @param offset The offset
     * @return true if the locally stored value has been set, false if the
     * requested value could not be stored locally.
     */
    fun setByte(value: Int, @IntRange(from = 0) offset: Int): Boolean {
        val len = offset + 1
        if (this.value == null) this.value = ByteArray(len)
        if (len > this.value!!.size) return false
        this.value!![offset] = value.toByte()
        return true
    }

    /**
     * Set the locally stored value of this data.
     *
     * See [.setValue] for details.
     *
     * @param value      New value for this data
     * @param formatType Integer format type used to transform the value parameter
     * @param offset     Offset at which the value should be placed
     * @return true if the locally stored value has been set
     */
    fun setValue(value: Int, @IntFormat formatType: Int, @IntRange(from = 0) offset: Int): Boolean {
        var value = value
        var offset = offset
        val len = offset + Data.getTypeLen(formatType)
        if (this.value == null) this.value = ByteArray(len)
        if (len > this.value!!.size) return false

        when (formatType) {
            Data.FORMAT_SINT8 -> {
                value = intToSignedBits(value, 8)
                this.value!![offset] = (value and 0xFF).toByte()
            }
            // Fall-through intended
            Data.FORMAT_UINT8 -> this.value!![offset] = (value and 0xFF).toByte()

            Data.FORMAT_SINT16 -> {
                value = intToSignedBits(value, 16)
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset] = (value shr 8 and 0xFF).toByte()
            }
            // Fall-through intended
            Data.FORMAT_UINT16 -> {
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset] = (value shr 8 and 0xFF).toByte()
            }

            Data.FORMAT_SINT24 -> {
                value = intToSignedBits(value, 24)
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset++] = (value shr 8 and 0xFF).toByte()
                this.value!![offset] = (value shr 16 and 0xFF).toByte()
            }
            // Fall-through intended
            Data.FORMAT_UINT24 -> {
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset++] = (value shr 8 and 0xFF).toByte()
                this.value!![offset] = (value shr 16 and 0xFF).toByte()
            }

            Data.FORMAT_SINT32 -> {
                value = intToSignedBits(value, 32)
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset++] = (value shr 8 and 0xFF).toByte()
                this.value!![offset++] = (value shr 16 and 0xFF).toByte()
                this.value!![offset] = (value shr 24 and 0xFF).toByte()
            }
            // Fall-through intended
            Data.FORMAT_UINT32 -> {
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset++] = (value shr 8 and 0xFF).toByte()
                this.value!![offset++] = (value shr 16 and 0xFF).toByte()
                this.value!![offset] = (value shr 24 and 0xFF).toByte()
            }

            else -> return false
        }
        return true
    }

    /**
     * Set the locally stored value of this data.
     *
     * See [.setValue] for details.
     *
     * @param mantissa   Mantissa for this data
     * @param exponent   Exponent value for this data
     * @param formatType Float format type used to transform the value parameter
     * @param offset     Offset at which the value should be placed
     * @return true if the locally stored value has been set
     */
    fun setValue(
        mantissa: Int, exponent: Int,
        @FloatFormat formatType: Int, @IntRange(from = 0) offset: Int
    ): Boolean {
        var mantissa = mantissa
        var exponent = exponent
        var offset = offset
        val len = offset + Data.getTypeLen(formatType)
        if (value == null) value = ByteArray(len)
        if (len > value!!.size) return false

        when (formatType) {
            Data.FORMAT_SFLOAT -> {
                mantissa = intToSignedBits(mantissa, 12)
                exponent = intToSignedBits(exponent, 4)
                value!![offset++] = (mantissa and 0xFF).toByte()
                value!![offset] = (mantissa shr 8 and 0x0F).toByte()
                value!![offset] = (value!![offset] + (exponent and 0x0F shl 4).toByte()).toByte()
            }

            Data.FORMAT_FLOAT -> {
                mantissa = intToSignedBits(mantissa, 24)
                exponent = intToSignedBits(exponent, 8)
                value!![offset++] = (mantissa and 0xFF).toByte()
                value!![offset++] = (mantissa shr 8 and 0xFF).toByte()
                value!![offset++] = (mantissa shr 16 and 0xFF).toByte()
                value!![offset] = (value!![offset] + (exponent and 0xFF).toByte()).toByte()
            }

            else -> return false
        }

        return true
    }

    /**
     * Set the locally stored value of this data.
     *
     * See [.setValue] for details.
     *
     * @param value      New value for this data. This allows to send [.FORMAT_UINT32].
     * @param formatType Integer format type used to transform the value parameter
     * @param offset     Offset at which the value should be placed
     * @return true if the locally stored value has been set
     */
    fun setValue(value: Long, @LongFormat formatType: Int, @IntRange(from = 0) offset: Int): Boolean {
        var value = value
        var offset = offset
        val len = offset + Data.getTypeLen(formatType)
        if (this.value == null) this.value = ByteArray(len)
        if (len > this.value!!.size) return false

        when (formatType) {
            Data.FORMAT_SINT32 -> {
                value = longToSignedBits(value, 32)
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset++] = (value shr 8 and 0xFF).toByte()
                this.value!![offset++] = (value shr 16 and 0xFF).toByte()
                this.value!![offset] = (value shr 24 and 0xFF).toByte()
            }
            // Fall-through intended
            Data.FORMAT_UINT32 -> {
                this.value!![offset++] = (value and 0xFF).toByte()
                this.value!![offset++] = (value shr 8 and 0xFF).toByte()
                this.value!![offset++] = (value shr 16 and 0xFF).toByte()
                this.value!![offset] = (value shr 24 and 0xFF).toByte()
            }

            else -> return false
        }
        return true
    }

    /**
     * Set the locally stored value of this data.
     *
     * See [.setValue] for details.
     *
     * @param value      Float value to be written
     * @param formatType Float format type used to transform the value parameter
     * @param offset     Offset at which the value should be placed
     * @return true if the locally stored value has been set
     */
    fun setValue(
        value: Float,
        @FloatFormat formatType: Int, @IntRange(from = 0) offset: Int
    ): Boolean {
        var offset = offset
        val len = offset + Data.getTypeLen(formatType)
        if (this.value == null) this.value = ByteArray(len)
        if (len > this.value!!.size) return false

        when (formatType) {
            Data.FORMAT_SFLOAT -> {
                val sfloatAsInt = sfloatToInt(value)
                this.value!![offset++] = (sfloatAsInt and 0xFF).toByte()
                this.value!![offset] = (sfloatAsInt shr 8 and 0xFF).toByte()
            }

            Data.FORMAT_FLOAT -> {
                val floatAsInt = floatToInt(value)
                this.value!![offset++] = (floatAsInt and 0xFF).toByte()
                this.value!![offset++] = (floatAsInt shr 8 and 0xFF).toByte()
                this.value!![offset++] = (floatAsInt shr 16 and 0xFF).toByte()
                this.value!![offset] =
                    (this.value!![offset] + (floatAsInt shr 24 and 0xFF).toByte()).toByte()
            }

            else -> return false
        }

        return true
    }

    companion object {
        // Values required to convert float to IEEE-11073 SFLOAT
        private val SFLOAT_POSITIVE_INFINITY = 0x07FE
        private val SFLOAT_NAN = 0x07FF
        // private final static int SFLOAT_NOT_AT_THIS_RESOLUTION = 0x0800;
        // private final static int SFLOAT_RESERVED_VALUE = 0x0801;
        private val SFLOAT_NEGATIVE_INFINITY = 0x0802
        private val SFLOAT_MANTISSA_MAX = 0x07FD
        private val SFLOAT_EXPONENT_MAX = 7
        private val SFLOAT_EXPONENT_MIN = -8
        private val SFLOAT_MAX = 20450000000.0f
        private val SFLOAT_MIN = -SFLOAT_MAX
        private val SFLOAT_PRECISION = 10000
        // private final static float SFLOAT_EPSILON = 1e-8f;

        // Values required to convert float to IEEE-11073 FLOAT
        private val FLOAT_POSITIVE_INFINITY = 0x007FFFFE
        private val FLOAT_NAN = 0x007FFFFF
        // private final static int FLOAT_NOT_AT_THIS_RESOLUTION = 0x00800000;
        // private final static int FLOAT_RESERVED_VALUE = 0x00800001;
        private val FLOAT_NEGATIVE_INFINITY = 0x00800002
        private val FLOAT_MANTISSA_MAX = 0x007FFFFD
        private val FLOAT_EXPONENT_MAX = 127
        private val FLOAT_EXPONENT_MIN = -128
        private val FLOAT_PRECISION = 10000000

        fun from(characteristic: BluetoothGattCharacteristic): MutableData {
            return MutableData(characteristic.value)
        }

        fun from(descriptor: BluetoothGattDescriptor): MutableData {
            return MutableData(descriptor.value)
        }

        /**
         * Converts float to SFLOAT IEEE 11073 format as UINT16, rounding up or down.
         * See: https://github.com/signove/antidote/blob/master/src/util/bytelib.c
         *
         * @param value the value to be converted.
         * @return given float as UINT16 in IEEE 11073 format.
         */
        private fun sfloatToInt(value: Float): Int {
            if (java.lang.Float.isNaN(value)) {
                return SFLOAT_NAN
            } else if (value > SFLOAT_MAX) {
                return SFLOAT_POSITIVE_INFINITY
            } else if (value < SFLOAT_MIN) {
                return SFLOAT_NEGATIVE_INFINITY
            }

            val sign = if (value >= 0) +1 else -1
            var mantissa = Math.abs(value)
            var exponent = 0 // Note: 10**x exponent, not 2**x

            // scale up if number is too big
            while (mantissa > SFLOAT_MANTISSA_MAX) {
                mantissa /= 10.0f
                ++exponent
                if (exponent > SFLOAT_EXPONENT_MAX) {
                    // argh, should not happen
                    return if (sign > 0) {
                        SFLOAT_POSITIVE_INFINITY
                    } else {
                        SFLOAT_NEGATIVE_INFINITY
                    }
                }
            }

            // scale down if number is too small
            while (mantissa < 1) {
                mantissa *= 10f
                --exponent
                if (exponent < SFLOAT_EXPONENT_MIN) {
                    // argh, should not happen
                    return 0
                }
            }

            // scale down if number needs more precision
            var smantissa = Math.round(mantissa * SFLOAT_PRECISION).toDouble()
            var rmantissa = (Math.round(mantissa) * SFLOAT_PRECISION).toDouble()
            var mdiff = Math.abs(smantissa - rmantissa)
            while (mdiff > 0.5 && exponent > SFLOAT_EXPONENT_MIN &&
                mantissa * 10 <= SFLOAT_MANTISSA_MAX
            ) {
                mantissa *= 10f
                --exponent
                smantissa = Math.round(mantissa * SFLOAT_PRECISION).toDouble()
                rmantissa = (Math.round(mantissa) * SFLOAT_PRECISION).toDouble()
                mdiff = Math.abs(smantissa - rmantissa)
            }

            val int_mantissa = Math.round(sign * mantissa)
            return exponent and 0xF shl 12 or (int_mantissa and 0xFFF)
        }

        /**
         * Converts float to FLOAT IEEE 11073 format as UINT32, rounding up or down.
         * See: https://github.com/signove/antidote/blob/master/src/util/bytelib.c
         *
         * @param value the value to be converted.
         * @return given float as UINT32 in IEEE 11073 format.
         */
        private fun floatToInt(value: Float): Int {
            if (java.lang.Float.isNaN(value)) {
                return FLOAT_NAN
            } else if (value == java.lang.Float.POSITIVE_INFINITY) {
                return FLOAT_POSITIVE_INFINITY
            } else if (value == java.lang.Float.NEGATIVE_INFINITY) {
                return FLOAT_NEGATIVE_INFINITY
            }

            val sign = if (value >= 0) +1 else -1
            var mantissa = Math.abs(value)
            var exponent = 0 // Note: 10**x exponent, not 2**x

            // scale up if number is too big
            while (mantissa > FLOAT_MANTISSA_MAX) {
                mantissa /= 10.0f
                ++exponent
                if (exponent > FLOAT_EXPONENT_MAX) {
                    // argh, should not happen
                    return if (sign > 0) {
                        FLOAT_POSITIVE_INFINITY
                    } else {
                        FLOAT_NEGATIVE_INFINITY
                    }
                }
            }

            // scale down if number is too small
            while (mantissa < 1) {
                mantissa *= 10f
                --exponent
                if (exponent < FLOAT_EXPONENT_MIN) {
                    // argh, should not happen
                    return 0
                }
            }

            // scale down if number needs more precision
            var smantissa = Math.round(mantissa * FLOAT_PRECISION).toDouble()
            var rmantissa = (Math.round(mantissa) * FLOAT_PRECISION).toDouble()
            var mdiff = Math.abs(smantissa - rmantissa)
            while (mdiff > 0.5 && exponent > FLOAT_EXPONENT_MIN &&
                mantissa * 10 <= FLOAT_MANTISSA_MAX
            ) {
                mantissa *= 10f
                --exponent
                smantissa = Math.round(mantissa * FLOAT_PRECISION).toDouble()
                rmantissa = (Math.round(mantissa) * FLOAT_PRECISION).toDouble()
                mdiff = Math.abs(smantissa - rmantissa)
            }

            val int_mantissa = Math.round(sign * mantissa)
            return exponent shl 24 or (int_mantissa and 0xFFFFFF)
        }

        /**
         * Convert an integer into the signed bits of a given length.
         */
        private fun intToSignedBits(i: Int, size: Int): Int {
            var i = i
            if (i < 0) {
                i = (1 shl size - 1) + (i and (1 shl size - 1) - 1)
            }
            return i
        }

        /**
         * Convert a long into the signed bits of a given length.
         */
        private fun longToSignedBits(i: Long, size: Int): Long {
            var i = i
            if (i < 0) {
                i = (1L shl size - 1) + (i and (1L shl size - 1) - 1)
            }
            return i
        }
    }
}
