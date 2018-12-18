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

package no.nordicsemi.android.ble.data;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings({"unused", "SameParameterValue", "WeakerAccess", "UnusedReturnValue"})
public class MutableData extends Data {
	// Values required to convert float to IEEE-11073 SFLOAT
	private final static int SFLOAT_POSITIVE_INFINITY = 0x07FE;
	private final static int SFLOAT_NAN = 0x07FF;
	// private final static int SFLOAT_NOT_AT_THIS_RESOLUTION = 0x0800;
	// private final static int SFLOAT_RESERVED_VALUE = 0x0801;
	private final static int SFLOAT_NEGATIVE_INFINITY = 0x0802;
	private final static int SFLOAT_MANTISSA_MAX = 0x07FD;
	private final static int SFLOAT_EXPONENT_MAX = 7;
	private final static int SFLOAT_EXPONENT_MIN = -8;
	private final static float SFLOAT_MAX = 20450000000.0f;
	private final static float SFLOAT_MIN = -SFLOAT_MAX;
	private final static int SFLOAT_PRECISION = 10000;
	// private final static float SFLOAT_EPSILON = 1e-8f;

	// Values required to convert float to IEEE-11073 FLOAT
	private final static int FLOAT_POSITIVE_INFINITY = 0x007FFFFE;
	private final static int FLOAT_NAN = 0x007FFFFF;
	// private final static int FLOAT_NOT_AT_THIS_RESOLUTION = 0x00800000;
	// private final static int FLOAT_RESERVED_VALUE = 0x00800001;
	private final static int FLOAT_NEGATIVE_INFINITY = 0x00800002;
	private final static int FLOAT_MANTISSA_MAX = 0x007FFFFD;
	private final static int FLOAT_EXPONENT_MAX = 127;
	private final static int FLOAT_EXPONENT_MIN = -128;
	private final static int FLOAT_PRECISION = 10000000;
	// private final static float FLOAT_EPSILON = 1e-128f;

	public MutableData() {
		super();
	}

	public MutableData(@Nullable final byte[] data) {
		super(data);
	}

	public static MutableData from(@NonNull final BluetoothGattCharacteristic characteristic) {
		return new MutableData(characteristic.getValue());
	}

	public static MutableData from(@NonNull final BluetoothGattDescriptor descriptor) {
		return new MutableData(descriptor.getValue());
	}

	/**
	 * Updates the locally stored value of this data.
	 *
	 * @param value New value
	 * @return true if the locally stored value has been set, false if the
	 * requested value could not be stored locally.
	 */
	public boolean setValue(@Nullable final byte[] value) {
		mValue = value;
		return true;
	}

	/**
	 * Updates the byte at offset position.
	 *
	 * @param value  Byte to set
	 * @param offset The offset
	 * @return true if the locally stored value has been set, false if the
	 * requested value could not be stored locally.
	 */
	public boolean setByte(final int value, @IntRange(from = 0) final int offset) {
		final int len = offset + 1;
		if (mValue == null) mValue = new byte[len];
		if (len > mValue.length) return false;
		mValue[offset] = (byte) value;
		return true;
	}

	/**
	 * Set the locally stored value of this data.
	 * <p>See {@link #setValue(byte[])} for details.
	 *
	 * @param value      New value for this data
	 * @param formatType Integer format type used to transform the value parameter
	 * @param offset     Offset at which the value should be placed
	 * @return true if the locally stored value has been set
	 */
	public boolean setValue(int value, @IntFormat int formatType, @IntRange(from = 0) int offset) {
		final int len = offset + getTypeLen(formatType);
		if (mValue == null) mValue = new byte[len];
		if (len > mValue.length) return false;

		switch (formatType) {
			case FORMAT_SINT8:
				value = intToSignedBits(value, 8);
				// Fall-through intended
			case FORMAT_UINT8:
				mValue[offset] = (byte) (value & 0xFF);
				break;

			case FORMAT_SINT16:
				value = intToSignedBits(value, 16);
				// Fall-through intended
			case FORMAT_UINT16:
				mValue[offset++] = (byte) (value & 0xFF);
				mValue[offset] = (byte) ((value >> 8) & 0xFF);
				break;

			case FORMAT_SINT24:
				value = intToSignedBits(value, 24);
				// Fall-through intended
			case FORMAT_UINT24:
				mValue[offset++] = (byte) (value & 0xFF);
				mValue[offset++] = (byte) ((value >> 8) & 0xFF);
				mValue[offset] = (byte) ((value >> 16) & 0xFF);
				break;

			case FORMAT_SINT32:
				value = intToSignedBits(value, 32);
				// Fall-through intended
			case FORMAT_UINT32:
				mValue[offset++] = (byte) (value & 0xFF);
				mValue[offset++] = (byte) ((value >> 8) & 0xFF);
				mValue[offset++] = (byte) ((value >> 16) & 0xFF);
				mValue[offset] = (byte) ((value >> 24) & 0xFF);
				break;

			default:
				return false;
		}
		return true;
	}

	/**
	 * Set the locally stored value of this data.
	 * <p>See {@link #setValue(byte[])} for details.
	 *
	 * @param mantissa   Mantissa for this data
	 * @param exponent   Exponent value for this data
	 * @param formatType Float format type used to transform the value parameter
	 * @param offset     Offset at which the value should be placed
	 * @return true if the locally stored value has been set
	 */
	public boolean setValue(int mantissa, int exponent,
							@FloatFormat int formatType, @IntRange(from = 0) int offset) {
		final int len = offset + getTypeLen(formatType);
		if (mValue == null) mValue = new byte[len];
		if (len > mValue.length) return false;

		switch (formatType) {
			case FORMAT_SFLOAT:
				mantissa = intToSignedBits(mantissa, 12);
				exponent = intToSignedBits(exponent, 4);
				mValue[offset++] = (byte) (mantissa & 0xFF);
				mValue[offset] = (byte) ((mantissa >> 8) & 0x0F);
				mValue[offset] += (byte) ((exponent & 0x0F) << 4);
				break;

			case FORMAT_FLOAT:
				mantissa = intToSignedBits(mantissa, 24);
				exponent = intToSignedBits(exponent, 8);
				mValue[offset++] = (byte) (mantissa & 0xFF);
				mValue[offset++] = (byte) ((mantissa >> 8) & 0xFF);
				mValue[offset++] = (byte) ((mantissa >> 16) & 0xFF);
				mValue[offset] += (byte) (exponent & 0xFF);
				break;

			default:
				return false;
		}

		return true;
	}

	/**
	 * Set the locally stored value of this data.
	 * <p>See {@link #setValue(byte[])} for details.
	 *
	 * @param value      New value for this data. This allows to send {@link #FORMAT_UINT32}.
	 * @param formatType Integer format type used to transform the value parameter
	 * @param offset     Offset at which the value should be placed
	 * @return true if the locally stored value has been set
	 */
	public boolean setValue(long value, @LongFormat int formatType, @IntRange(from = 0) int offset) {
		final int len = offset + getTypeLen(formatType);
		if (mValue == null) mValue = new byte[len];
		if (len > mValue.length) return false;

		switch (formatType) {
			case FORMAT_SINT32:
				value = longToSignedBits(value, 32);
				// Fall-through intended
			case FORMAT_UINT32:
				mValue[offset++] = (byte) (value & 0xFF);
				mValue[offset++] = (byte) ((value >> 8) & 0xFF);
				mValue[offset++] = (byte) ((value >> 16) & 0xFF);
				mValue[offset] = (byte) ((value >> 24) & 0xFF);
				break;

			default:
				return false;
		}
		return true;
	}

	/**
	 * Set the locally stored value of this data.
	 * <p>See {@link #setValue(byte[])} for details.
	 *
	 * @param value      Float value to be written
	 * @param formatType Float format type used to transform the value parameter
	 * @param offset     Offset at which the value should be placed
	 * @return true if the locally stored value has been set
	 */
	public boolean setValue(float value,
							@FloatFormat int formatType, @IntRange(from = 0) int offset) {
		final int len = offset + getTypeLen(formatType);
		if (mValue == null) mValue = new byte[len];
		if (len > mValue.length) return false;

		switch (formatType) {
			case FORMAT_SFLOAT:
				final int sfloatAsInt = sfloatToInt(value);
				mValue[offset++] = (byte) (sfloatAsInt & 0xFF);
				mValue[offset] = (byte) ((sfloatAsInt >> 8) & 0xFF);
				break;

			case FORMAT_FLOAT:
				final int floatAsInt = floatToInt(value);
				mValue[offset++] = (byte) (floatAsInt & 0xFF);
				mValue[offset++] = (byte) ((floatAsInt >> 8) & 0xFF);
				mValue[offset++] = (byte) ((floatAsInt >> 16) & 0xFF);
				mValue[offset] += (byte) ((floatAsInt >> 24) & 0xFF);
				break;

			default:
				return false;
		}

		return true;
	}

	/**
	 * Converts float to SFLOAT IEEE 11073 format as UINT16, rounding up or down.
	 * See: https://github.com/signove/antidote/blob/master/src/util/bytelib.c
	 *
	 * @param value the value to be converted.
	 * @return given float as UINT16 in IEEE 11073 format.
	 */
	private static int sfloatToInt(final float value) {
		if (Float.isNaN(value)) {
			return SFLOAT_NAN;
		} else if (value > SFLOAT_MAX) {
			return SFLOAT_POSITIVE_INFINITY;
		} else if (value < SFLOAT_MIN) {
			return SFLOAT_NEGATIVE_INFINITY;
		}

		int sign = value >= 0 ? +1 : -1;
		float mantissa = Math.abs(value);
		int exponent = 0; // Note: 10**x exponent, not 2**x

		// scale up if number is too big
		while (mantissa > SFLOAT_MANTISSA_MAX) {
			mantissa /= 10.0f;
			++exponent;
			if (exponent > SFLOAT_EXPONENT_MAX) {
				// argh, should not happen
				if (sign > 0) {
					return SFLOAT_POSITIVE_INFINITY;
				} else {
					return SFLOAT_NEGATIVE_INFINITY;
				}
			}
		}

		// scale down if number is too small
		while (mantissa < 1) {
			mantissa *= 10;
			--exponent;
			if (exponent < SFLOAT_EXPONENT_MIN) {
				// argh, should not happen
				return 0;
			}
		}

		// scale down if number needs more precision
		double smantissa = Math.round(mantissa * SFLOAT_PRECISION);
		double rmantissa = Math.round(mantissa) * SFLOAT_PRECISION;
		double mdiff = Math.abs(smantissa - rmantissa);
		while (mdiff > 0.5 && exponent > SFLOAT_EXPONENT_MIN &&
				(mantissa * 10) <= SFLOAT_MANTISSA_MAX) {
			mantissa *= 10;
			--exponent;
			smantissa = Math.round(mantissa * SFLOAT_PRECISION);
			rmantissa = Math.round(mantissa) * SFLOAT_PRECISION;
			mdiff = Math.abs(smantissa - rmantissa);
		}

		int int_mantissa = Math.round(sign * mantissa);
		return ((exponent & 0xF) << 12) | (int_mantissa & 0xFFF);
	}

	/**
	 * Converts float to FLOAT IEEE 11073 format as UINT32, rounding up or down.
	 * See: https://github.com/signove/antidote/blob/master/src/util/bytelib.c
	 *
	 * @param value the value to be converted.
	 * @return given float as UINT32 in IEEE 11073 format.
	 */
	private static int floatToInt(final float value) {
		if (Float.isNaN(value)) {
			return FLOAT_NAN;
		} else if (value == Float.POSITIVE_INFINITY) {
			return FLOAT_POSITIVE_INFINITY;
		} else if (value == Float.NEGATIVE_INFINITY) {
			return FLOAT_NEGATIVE_INFINITY;
		}

		int sign = value >= 0 ? +1 : -1;
		float mantissa = Math.abs(value);
		int exponent = 0; // Note: 10**x exponent, not 2**x

		// scale up if number is too big
		while (mantissa > FLOAT_MANTISSA_MAX) {
			mantissa /= 10.0f;
			++exponent;
			if (exponent > FLOAT_EXPONENT_MAX) {
				// argh, should not happen
				if (sign > 0) {
					return FLOAT_POSITIVE_INFINITY;
				} else {
					return FLOAT_NEGATIVE_INFINITY;
				}
			}
		}

		// scale down if number is too small
		while (mantissa < 1) {
			mantissa *= 10;
			--exponent;
			if (exponent < FLOAT_EXPONENT_MIN) {
				// argh, should not happen
				return 0;
			}
		}

		// scale down if number needs more precision
		double smantissa = Math.round(mantissa * FLOAT_PRECISION);
		double rmantissa = Math.round(mantissa) * FLOAT_PRECISION;
		double mdiff = Math.abs(smantissa - rmantissa);
		while (mdiff > 0.5 && exponent > FLOAT_EXPONENT_MIN &&
				(mantissa * 10) <= FLOAT_MANTISSA_MAX) {
			mantissa *= 10;
			--exponent;
			smantissa = Math.round(mantissa * FLOAT_PRECISION);
			rmantissa = Math.round(mantissa) * FLOAT_PRECISION;
			mdiff = Math.abs(smantissa - rmantissa);
		}

		int int_mantissa = Math.round(sign * mantissa);
		return (exponent << 24) | (int_mantissa & 0xFFFFFF);
	}

	/**
	 * Convert an integer into the signed bits of a given length.
	 */
	private static int intToSignedBits(int i, int size) {
		if (i < 0) {
			i = (1 << size - 1) + (i & ((1 << size - 1) - 1));
		}
		return i;
	}

	/**
	 * Convert a long into the signed bits of a given length.
	 */
	private static long longToSignedBits(long i, int size) {
		if (i < 0) {
			i = (1L << size - 1) + (i & ((1L << size - 1) - 1));
		}
		return i;
	}
}
