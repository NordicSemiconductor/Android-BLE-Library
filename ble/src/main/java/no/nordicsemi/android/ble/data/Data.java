package no.nordicsemi.android.ble.data;

@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public final class Data {
	private static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	/**
	 * Data value format type uint8
	 */
	public final static int FORMAT_UINT8 = 0x11;

	/**
	 * Data value format type uint16
	 */
	public final static int FORMAT_UINT16 = 0x12;

	/**
	 * Data value format type uint24
	 */
	public final static int FORMAT_UINT24 = 0x13;

	/**
	 * Data value format type uint32
	 */
	public final static int FORMAT_UINT32 = 0x14;

	/**
	 * Data value format type sint8
	 */
	public final static int FORMAT_SINT8 = 0x21;

	/**
	 * Data value format type sint16
	 */
	public final static int FORMAT_SINT16 = 0x22;

	/**
	 * Data value format type sint24
	 */
	public final static int FORMAT_SINT24 = 0x23;

	/**
	 * Data value format type sint32
	 */
	public final static int FORMAT_SINT32 = 0x24;

	/**
	 * Data value format type sfloat (16-bit float, IEEE-11073)
	 */
	public final static int FORMAT_SFLOAT = 0x32;

	/**
	 * Data value format type float (32-bit float, IEEE-11073)
	 */
	public final static int FORMAT_FLOAT = 0x34;

	// Values required to convert float to IEEE-11073 SFLOAT
	private final static int SFLOAT_POSITIVE_INFINITY = 0x07FE;
	private final static int SFLOAT_NAN = 0x07FF;
	private final static int SFLOAT_NOT_AT_THIS_RESOLUTION = 0x0800;
	private final static int SFLOAT_RESERVED_VALUE = 0x0801;
	private final static int SFLOAT_NEGATIVE_INFINITY = 0x0802;
	private final static int SFLOAT_MANTISSA_MAX = 0x07FD;
	private final static int SFLOAT_EXPONENT_MAX = 7;
	private final static int SFLOAT_EXPONENT_MIN = -8;
	private final static float SFLOAT_MAX = 20450000000.0f;
	private final static float SFLOAT_MIN = -SFLOAT_MAX;
	private final static int SFLOAT_PRECISION = 10000;
	private final static float SFLOAT_EPSILON = 1e-8f;

	// Values required to convert float to IEEE-11073 FLOAT
	private final static int FLOAT_POSITIVE_INFINITY = 0x007FFFFE;
	private final static int FLOAT_NAN = 0x007FFFFF;
	private final static int FLOAT_NOT_AT_THIS_RESOLUTION = 0x00800000;
	private final static int FLOAT_RESERVED_VALUE = 0x00800001;
	private final static int FLOAT_NEGATIVE_INFINITY = 0x00800002;
	private final static int FLOAT_MANTISSA_MAX = 0x007FFFFD;
	private final static int FLOAT_EXPONENT_MAX = 127;
	private final static int FLOAT_EXPONENT_MIN = -128;
	private final static int FLOAT_PRECISION = 10000000;

	private byte[] mValue;

	public Data() {
		this.mValue = null;
	}

	public Data(final byte[] value) {
		this.mValue = value;
	}

	public Data(final int size) {
		this.mValue = new byte[size];
	}

	public static Data opCode(final byte opCode) {
		return new Data(new byte[]{opCode});
	}

	public static Data opCode(final byte opCode, final byte parameter) {
		return new Data(new byte[]{opCode, parameter});
	}

	public static Data opCode(final byte opCode, final int parameter, int formatType) {
		final Data data = new Data(new byte[1 + getTypeLen(formatType)]);
		data.setByte(opCode, 0);
		data.setValue(parameter, formatType, 1);
		return data;
	}

	/**
	 * Returns the underlying byte array.
	 *
	 * @return Data received.
	 */
	public byte[] getValue() {
		return mValue;
	}

	/**
	 * Returns the size of underlying byte array.
	 *
	 * @return Length of the data.
	 */
	public int size() {
		return mValue != null ? mValue.length : 0;
	}

	@Override
	public String toString() {
		if (size() == 0)
			return "";

		final char[] out = new char[mValue.length * 3 - 1];
		for (int j = 0; j < mValue.length; j++) {
			int v = mValue[j] & 0xFF;
			out[j * 3] = HEX_ARRAY[v >>> 4];
			out[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
			if (j != mValue.length - 1)
				out[j * 3 + 2] = '-';
		}
		return "(0x) " + new String(out);
	}

	/**
	 * Returns a byte at the given offset from the byte array.
	 *
	 * @param offset Offset at which the byte value can be found.
	 * @return Cached value or null of offset exceeds value size.
	 */
	public Byte getByte(final int offset) {
		if (offset + 1 > size()) return null;

		return mValue[offset];
	}

	/**
	 * Returns an integer value from the byte array.
	 * <p>
	 * <p>The formatType parameter determines how the value
	 * is to be interpreted. For example, setting formatType to
	 * {@link #FORMAT_UINT16} specifies that the first two bytes of the
	 * value at the given offset are interpreted to generate the
	 * return value.
	 *
	 * @param formatType The format type used to interpret the value.
	 * @param offset     Offset at which the integer value can be found.
	 * @return Cached value or null of offset exceeds value size.
	 */
	public Integer getIntValue(final int formatType, final int offset) {
		if ((offset + getTypeLen(formatType)) > size()) return null;

		switch (formatType) {
			case FORMAT_UINT8:
				return unsignedByteToInt(mValue[offset]);

			case FORMAT_UINT16:
				return unsignedBytesToInt(mValue[offset], mValue[offset + 1]);

			case FORMAT_UINT24:
				return unsignedBytesToInt(mValue[offset], mValue[offset + 1],
						mValue[offset + 2], (byte) 0);

			case FORMAT_UINT32:
				return unsignedBytesToInt(mValue[offset], mValue[offset + 1],
						mValue[offset + 2], mValue[offset + 3]);

			case FORMAT_SINT8:
				return unsignedToSigned(unsignedByteToInt(mValue[offset]), 8);

			case FORMAT_SINT16:
				return unsignedToSigned(unsignedBytesToInt(mValue[offset],
						mValue[offset + 1]), 16);

			case FORMAT_SINT24:
				return unsignedToSigned(unsignedBytesToInt(mValue[offset],
						mValue[offset + 1], mValue[offset + 2], (byte) 0), 24);

			case FORMAT_SINT32:
				return unsignedToSigned(unsignedBytesToInt(mValue[offset],
						mValue[offset + 1], mValue[offset + 2], mValue[offset + 3]), 32);
		}

		return null;
	}

	/**
	 * Returns an float value from the given byte array.
	 *
	 * @param formatType The format type used to interpret the value.
	 * @param offset     Offset at which the float value can be found.
	 * @return Cached value at a given offset or null if the requested offset exceeds the value size.
	 */
	public Float getFloatValue(final int formatType, final int offset) {
		if ((offset + getTypeLen(formatType)) > size()) return null;

		switch (formatType) {
			case FORMAT_SFLOAT:
				if (mValue[offset + 1] == 0x07 && mValue[offset] == (byte) 0xFE)
					return Float.POSITIVE_INFINITY;
				if ((mValue[offset + 1] == 0x07 && mValue[offset] == (byte) 0xFF) ||
					(mValue[offset + 1] == 0x08 && mValue[offset] == 0x00) ||
					(mValue[offset + 1] == 0x08 && mValue[offset] == 0x01))
					return Float.NaN;
				if (mValue[offset + 1] == 0x08 && mValue[offset] == 0x02)
					return Float.NEGATIVE_INFINITY;

				return bytesToFloat(mValue[offset], mValue[offset + 1]);

			case FORMAT_FLOAT:
				if (mValue[offset + 3] == 0x00) {
					if (mValue[offset + 2] == 0x7F && mValue[offset + 1] == (byte) 0xFF) {
						if (mValue[offset] == (byte) 0xFE)
							return Float.POSITIVE_INFINITY;
						if (mValue[offset] == (byte) 0xFF)
							return Float.NaN;
					} else if (mValue[offset + 2] == (byte) 0x80 && mValue[offset + 1] == 0x00) {
						if (mValue[offset] == 0x00 || mValue[offset] == 0x01)
							return Float.NaN;
						if (mValue[offset] == 0x02)
							return Float.NEGATIVE_INFINITY;
					}
				}

				return bytesToFloat(mValue[offset], mValue[offset + 1],
						mValue[offset + 2], mValue[offset + 3]);
		}

		return null;
	}

	/**
	 * Updates the locally stored value of this data.
	 *
	 * @param value New value
	 * @return true if the locally stored value has been set, false if the
	 * requested value could not be stored locally.
	 */
	public boolean setValue(final byte[] value) {
		mValue = value;
		return true;
	}

	/**
	 * Updates the byte at offset position.
	 *
	 * @param value  byte to set
	 * @param offset the offset
	 * @return true if the locally stored value has been set, false if the
	 * requested value could not be stored locally.
	 */
	public boolean setByte(final int value, final int offset) {
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
	public boolean setValue(int value, int formatType, int offset) {
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
	public boolean setValue(int mantissa, int exponent, int formatType, int offset) {
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
	 * @param value      Float value to be written
	 * @param formatType Float format type used to transform the value parameter
	 * @param offset     Offset at which the value should be placed
	 * @return true if the locally stored value has been set
	 */
	public boolean setValue(float value, int formatType, int offset) {
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
	 * Returns the size of a give value type.
	 */
	private static int getTypeLen(int formatType) {
		return formatType & 0xF;
	}

	/**
	 * Convert a signed byte to an unsigned int.
	 */
	private static int unsignedByteToInt(byte b) {
		return b & 0xFF;
	}

	/**
	 * Convert signed bytes to a 16-bit unsigned int.
	 */
	private static int unsignedBytesToInt(byte b0, byte b1) {
		return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
	}

	/**
	 * Convert signed bytes to a 32-bit unsigned int.
	 */
	private static int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
		return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
				+ (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
	}

	/**
	 * Convert signed bytes to a 16-bit short float value.
	 */
	private static float bytesToFloat(byte b0, byte b1) {
		int mantissa = unsignedToSigned(unsignedByteToInt(b0)
				+ ((unsignedByteToInt(b1) & 0x0F) << 8), 12);
		int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);
		return (float) (mantissa * Math.pow(10, exponent));
	}

	/**
	 * Convert signed bytes to a 32-bit short float value.
	 */
	private static float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
		int mantissa = unsignedToSigned(unsignedByteToInt(b0)
				+ (unsignedByteToInt(b1) << 8)
				+ (unsignedByteToInt(b2) << 16), 24);
		return (float) (mantissa * Math.pow(10, b3));
	}

	/**
	 * Convert an unsigned integer value to a two's-complement encoded
	 * signed value.
	 */
	private static int unsignedToSigned(int unsigned, int size) {
		if ((unsigned & (1 << size - 1)) != 0) {
			unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
		}
		return unsigned;
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
}
