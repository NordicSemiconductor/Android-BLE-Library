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
import android.os.Parcel
import android.os.Parcelable

import androidx.annotation.IntDef
import androidx.annotation.IntRange

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.experimental.and

open class Data : Parcelable {
    /**
     * Returns the underlying byte array.
     *
     * @return Data received.
     */
    var value: ByteArray? = null
        protected set

    constructor() {
        this.value = null
    }

    constructor(value: ByteArray?) {
        this.value = value
    }

    // Parcelable
    protected constructor(`in`: Parcel) {
        value = `in`.createByteArray()
    }

    /**
     * Return the stored value of this characteristic.
     *
     * See [.getValue] for details.
     *
     * @param offset Offset at which the string value can be found.
     * @return Cached value of the characteristic
     */
    fun getStringValue(@IntRange(from = 0) offset: Int): String? {
        if (value == null || offset > value!!.size)
            return null
        val strBytes = ByteArray(value!!.size - offset)
        for (i in 0 until value!!.size - offset)
            strBytes[i] = value!![offset + i]
        return String(strBytes)
    }

    /**
     * Returns the size of underlying byte array.
     *
     * @return Length of the data.
     */
    fun size(): Int {
        return if (value != null) value!!.size else 0
    }

    override fun toString(): String {
        if (size() == 0)
            return ""

        val out = CharArray(value!!.size * 3 - 1)
        for (j in value!!.indices) {
            val v = value!![j] and 0xFF.toByte()
            out[j * 3] = HEX_ARRAY[v.toInt().ushr(4)]
            out[j * 3 + 1] = HEX_ARRAY[(v and 0x0F).toInt()]
            if (j != value!!.size - 1)
                out[j * 3 + 2] = '-'
        }
        return "(0x) " + String(out)
    }

    /**
     * Returns a byte at the given offset from the byte array.
     *
     * @param offset Offset at which the byte value can be found.
     * @return Cached value or null of offset exceeds value size.
     */
    fun getByte(@IntRange(from = 0) offset: Int): Byte? {
        return if (offset + 1 > size()) null else value!![offset]

    }

    /**
     * Returns an integer value from the byte array.
     *
     *
     *
     * The formatType parameter determines how the value
     * is to be interpreted. For example, setting formatType to
     * [.FORMAT_UINT16] specifies that the first two bytes of the
     * value at the given offset are interpreted to generate the
     * return value.
     *
     * @param formatType The format type used to interpret the value.
     * @param offset     Offset at which the integer value can be found.
     * @return Cached value or null of offset exceeds value size.
     */
    fun getIntValue(
        @IntFormat formatType: Int,
        @IntRange(from = 0) offset: Int
    ): Int? {
        if (offset + getTypeLen(formatType) > size()) return null

        when (formatType) {
            FORMAT_UINT8 -> return unsignedByteToInt(value!![offset])

            FORMAT_UINT16 -> return unsignedBytesToInt(value!![offset], value!![offset + 1])

            FORMAT_UINT24 -> return unsignedBytesToInt(
                value!![offset], value!![offset + 1],
                value!![offset + 2], 0.toByte()
            )

            FORMAT_UINT32 -> return unsignedBytesToInt(
                value!![offset], value!![offset + 1],
                value!![offset + 2], value!![offset + 3]
            )

            FORMAT_SINT8 -> return unsignedToSigned(unsignedByteToInt(value!![offset]), 8)

            FORMAT_SINT16 -> return unsignedToSigned(
                unsignedBytesToInt(
                    value!![offset],
                    value!![offset + 1]
                ), 16
            )

            FORMAT_SINT24 -> return unsignedToSigned(
                unsignedBytesToInt(
                    value!![offset],
                    value!![offset + 1], value!![offset + 2], 0.toByte()
                ), 24
            )

            FORMAT_SINT32 -> return unsignedToSigned(
                unsignedBytesToInt(
                    value!![offset],
                    value!![offset + 1], value!![offset + 2], value!![offset + 3]
                ), 32
            )
        }

        return null
    }

    /**
     * Returns a long value from the byte array.
     *
     * Only [.FORMAT_UINT32] and [.FORMAT_SINT32] are supported.
     *
     * The formatType parameter determines how the value
     * is to be interpreted. For example, setting formatType to
     * [.FORMAT_UINT32] specifies that the first four bytes of the
     * value at the given offset are interpreted to generate the
     * return value.
     *
     * @param formatType The format type used to interpret the value.
     * @param offset     Offset at which the integer value can be found.
     * @return Cached value or null of offset exceeds value size.
     */
    fun getLongValue(
        @LongFormat formatType: Int,
        @IntRange(from = 0) offset: Int
    ): Long? {
        if (offset + getTypeLen(formatType) > size()) return null

        when (formatType) {
            FORMAT_SINT32 -> return unsignedToSigned(
                unsignedBytesToLong(
                    value!![offset],
                    value!![offset + 1], value!![offset + 2], value!![offset + 3]
                ), 32
            )

            FORMAT_UINT32 -> return unsignedBytesToLong(
                value!![offset], value!![offset + 1],
                value!![offset + 2], value!![offset + 3]
            )
        }

        return null
    }

    /**
     * Returns an float value from the given byte array.
     *
     * @param formatType The format type used to interpret the value.
     * @param offset     Offset at which the float value can be found.
     * @return Cached value at a given offset or null if the requested offset exceeds the value size.
     */
    fun getFloatValue(
        @FloatFormat formatType: Int,
        @IntRange(from = 0) offset: Int
    ): Float? {
        if (offset + getTypeLen(formatType) > size()) return null

        when (formatType) {
            FORMAT_SFLOAT -> {
                if (value!![offset + 1].toInt() == 0x07 && value!![offset] == 0xFE.toByte())
                    return java.lang.Float.POSITIVE_INFINITY
                if (value!![offset + 1].toInt() == 0x07 && value!![offset] == 0xFF.toByte() ||
                    value!![offset + 1].toInt() == 0x08 && value!![offset].toInt() == 0x00 ||
                    value!![offset + 1].toInt() == 0x08 && value!![offset].toInt() == 0x01
                )
                    return java.lang.Float.NaN
                return if (value!![offset + 1].toInt() == 0x08 && value!![offset].toInt() == 0x02) java.lang.Float.NEGATIVE_INFINITY else bytesToFloat(
                    value!![offset],
                    value!![offset + 1]
                )

            }

            FORMAT_FLOAT -> {
                if (value!![offset + 3].toInt() == 0x00) {
                    if (value!![offset + 2].toInt() == 0x7F && value!![offset + 1] == 0xFF.toByte()) {
                        if (value!![offset] == 0xFE.toByte())
                            return java.lang.Float.POSITIVE_INFINITY
                        if (value!![offset] == 0xFF.toByte())
                            return java.lang.Float.NaN
                    } else if (value!![offset + 2] == 0x80.toByte() && value!![offset + 1].toInt() == 0x00) {
                        if (value!![offset].toInt() == 0x00 || value!![offset].toInt() == 0x01)
                            return java.lang.Float.NaN
                        if (value!![offset].toInt() == 0x02)
                            return java.lang.Float.NEGATIVE_INFINITY
                    }
                }

                return bytesToFloat(
                    value!![offset], value!![offset + 1],
                    value!![offset + 2], value!![offset + 3]
                )
            }
        }

        return null
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByteArray(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = [FORMAT_UINT8, FORMAT_UINT16, FORMAT_UINT24, FORMAT_UINT32, FORMAT_SINT8, FORMAT_SINT16, FORMAT_SINT24, FORMAT_SINT32, FORMAT_FLOAT, FORMAT_SFLOAT])
    annotation class ValueFormat

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = [FORMAT_UINT8, FORMAT_UINT16, FORMAT_UINT24, FORMAT_UINT32, FORMAT_SINT8, FORMAT_SINT16, FORMAT_SINT24, FORMAT_SINT32])
    annotation class IntFormat

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = [FORMAT_UINT32, FORMAT_SINT32])
    annotation class LongFormat

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = [FORMAT_FLOAT, FORMAT_SFLOAT])
    annotation class FloatFormat

    companion object {
        /**
         * Data value format type uint8
         */
        const val FORMAT_UINT8 = 0x11
        /**
         * Data value format type uint16
         */
        const val FORMAT_UINT16 = 0x12
        /**
         * Data value format type uint24
         */
        const val FORMAT_UINT24 = 0x13
        /**
         * Data value format type uint32
         */
        const val FORMAT_UINT32 = 0x14
        /**
         * Data value format type sint8
         */
        const val FORMAT_SINT8 = 0x21
        /**
         * Data value format type sint16
         */
        const val FORMAT_SINT16 = 0x22
        /**
         * Data value format type sint24
         */
        const val FORMAT_SINT24 = 0x23
        /**
         * Data value format type sint32
         */
        const val FORMAT_SINT32 = 0x24
        /**
         * Data value format type sfloat (16-bit float, IEEE-11073)
         */
        const val FORMAT_SFLOAT = 0x32
        /**
         * Data value format type float (32-bit float, IEEE-11073)
         */
        const val FORMAT_FLOAT = 0x34
        @JvmField
        val CREATOR: Parcelable.Creator<Data> = object : Parcelable.Creator<Data> {
            override fun createFromParcel(`in`: Parcel): Data {
                return Data(`in`)
            }

            override fun newArray(size: Int): Array<Data?> {
                return arrayOfNulls(size)
            }
        }
        private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

        fun from(value: String): Data {
            return Data(value.toByteArray()) // UTF-8
        }

        fun from(characteristic: BluetoothGattCharacteristic): Data {
            return Data(characteristic.value)
        }

        fun from(descriptor: BluetoothGattDescriptor): Data {
            return Data(descriptor.value)
        }

        fun opCode(opCode: Byte): Data {
            return Data(byteArrayOf(opCode))
        }

        fun opCode(opCode: Byte, parameter: Byte): Data {
            return Data(byteArrayOf(opCode, parameter))
        }

        /**
         * Returns the size of a give value type.
         */
        fun getTypeLen(@ValueFormat formatType: Int): Int {
            return formatType and 0xF
        }

        /**
         * Convert a signed byte to an unsigned int.
         */
        private fun unsignedByteToInt(b: Byte): Int {
            return (b and 0xFF.toByte()).toInt()
        }

        /**
         * Convert a signed byte to an unsigned int.
         */
        private fun unsignedByteToLong(b: Byte): Long {
            return (b and 0xFFL.toByte()).toLong()
        }

        /**
         * Convert signed bytes to a 16-bit unsigned int.
         */
        private fun unsignedBytesToInt(b0: Byte, b1: Byte): Int {
            return unsignedByteToInt(b0) + (unsignedByteToInt(b1) shl 8)
        }

        /**
         * Convert signed bytes to a 32-bit unsigned int.
         */
        private fun unsignedBytesToInt(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Int {
            return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) shl 8)
                    + (unsignedByteToInt(b2) shl 16) + (unsignedByteToInt(b3) shl 24))
        }

        /**
         * Convert signed bytes to a 32-bit unsigned long.
         */
        private fun unsignedBytesToLong(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Long {
            return (unsignedByteToLong(b0) + (unsignedByteToLong(b1) shl 8)
                    + (unsignedByteToLong(b2) shl 16) + (unsignedByteToLong(b3) shl 24))
        }

        /**
         * Convert signed bytes to a 16-bit short float value.
         */
        private fun bytesToFloat(b0: Byte, b1: Byte): Float {
            val mantissa =
                unsignedToSigned(unsignedByteToInt(b0) + (unsignedByteToInt(b1) and 0x0F shl 8), 12)
            val exponent = unsignedToSigned(unsignedByteToInt(b1) shr 4, 4)
            return (mantissa * Math.pow(10.0, exponent.toDouble())).toFloat()
        }

        /**
         * Convert signed bytes to a 32-bit short float value.
         */
        private fun bytesToFloat(b0: Byte, b1: Byte, b2: Byte, b3: Byte): Float {
            val mantissa = unsignedToSigned(
                unsignedByteToInt(b0)
                        + (unsignedByteToInt(b1) shl 8)
                        + (unsignedByteToInt(b2) shl 16), 24
            )
            return (mantissa * Math.pow(10.0, b3.toDouble())).toFloat()
        }

        /**
         * Convert an unsigned integer value to a two's-complement encoded
         * signed value.
         */
        private fun unsignedToSigned(unsigned: Int, size: Int): Int {
            var unsigned = unsigned
            if (unsigned and (1 shl size - 1) != 0) {
                unsigned = -1 * ((1 shl size - 1) - (unsigned and (1 shl size - 1) - 1))
            }
            return unsigned
        }

        /**
         * Convert an unsigned long value to a two's-complement encoded
         * signed value.
         */
        private fun unsignedToSigned(unsigned: Long, size: Int): Long {
            var unsigned = unsigned
            if (unsigned and (1 shl size - 1).toLong() != 0L) {
                unsigned = -1 * ((1 shl size - 1) - (unsigned and (1 shl size - 1).toLong() - 1))
            }
            return unsigned
        }
    }
}
