package no.nordicsemi.android.ble.callback;

public class Data {

	/**
	 * Characteristic value format type uint8
	 */
	public final static int FORMAT_UINT8 = 0x11;

	/**
	 * Characteristic value format type uint16
	 */
	public final static int FORMAT_UINT16 = 0x12;

	/**
	 * Characteristic value format type uint32
	 */
	public final static int FORMAT_UINT32 = 0x14;

	/**
	 * Characteristic value format type sint8
	 */
	public final static int FORMAT_SINT8 = 0x21;

	/**
	 * Characteristic value format type sint16
	 */
	public final static int FORMAT_SINT16 = 0x22;

	/**
	 * Characteristic value format type sint32
	 */
	public final static int FORMAT_SINT32 = 0x24;

	/**
	 * Characteristic value format type sfloat (16-bit float)
	 */
	public final static int FORMAT_SFLOAT = 0x32;

	/**
	 * Characteristic value format type float (32-bit float)
	 */
	public final static int FORMAT_FLOAT = 0x34;

	private final byte[] mValue;

	public Data(final byte[] value) {
		this.mValue = value;
	}

	/**
	 * Returns the underlying byte array.
	 * @return data received
	 */
	public byte[] getValue() {
		return mValue;
	}

	/**
	 * Returns a byte at the given offset from the byte array.
	 * @param offset Offset at which the byte value can be found.
	 * @return Cached value or null of offset exceeds value size.
	 */
	public Byte getByte(final int offset) {
		if (offset + 1 > mValue.length) return null;

		return mValue[offset];
	}

	/**
	 * Returns an integer value from the byte array.
	 *
	 * <p>The formatType parameter determines how the value
	 * is to be interpreted. For example, setting formatType to
	 * {@link #FORMAT_UINT16} specifies that the first two bytes of the
	 * value at the given offset are interpreted to generate the
	 * return value.
	 *
	 * @param formatType The format type used to interpret the value.
	 * @param offset Offset at which the integer value can be found.
	 * @return Cached value or null of offset exceeds value size.
	 */
	public Integer getIntValue(final int formatType, final int offset) {
		if ((offset + getTypeLen(formatType)) > mValue.length) return null;

		switch (formatType) {
			case FORMAT_UINT8:
				return unsignedByteToInt(mValue[offset]);

			case FORMAT_UINT16:
				return unsignedBytesToInt(mValue[offset], mValue[offset+1]);

			case FORMAT_UINT32:
				return unsignedBytesToInt(mValue[offset],   mValue[offset+1],
						mValue[offset+2], mValue[offset+3]);
			case FORMAT_SINT8:
				return unsignedToSigned(unsignedByteToInt(mValue[offset]), 8);

			case FORMAT_SINT16:
				return unsignedToSigned(unsignedBytesToInt(mValue[offset],
						mValue[offset+1]), 16);

			case FORMAT_SINT32:
				return unsignedToSigned(unsignedBytesToInt(mValue[offset],
						mValue[offset+1], mValue[offset+2], mValue[offset+3]), 32);
		}

		return null;
	}

	/**
	 * Returns an float value from the given byte array.
	 *
	 * @param formatType The format type used to interpret the value.
	 * @param offset Offset at which the float value can be found.
	 * @return Cached value at a given offset or null if the requested offset exceeds the value size.
	 */
	public Float getFloatValue(final int formatType, final int offset) {
		if ((offset + getTypeLen(formatType)) > mValue.length) return null;

		switch (formatType) {
			case FORMAT_SFLOAT:
				return bytesToFloat(mValue[offset], mValue[offset+1]);

			case FORMAT_FLOAT:
				return bytesToFloat(mValue[offset],   mValue[offset+1],
						mValue[offset+2], mValue[offset+3]);
		}

		return null;
	}

	/**
	 * Returns the size of a give value type.
	 */
	private int getTypeLen(int formatType) {
		return formatType & 0xF;
	}

	/**
	 * Convert a signed byte to an unsigned int.
	 */
	private int unsignedByteToInt(byte b) {
		return b & 0xFF;
	}

	/**
	 * Convert signed bytes to a 16-bit unsigned int.
	 */
	private int unsignedBytesToInt(byte b0, byte b1) {
		return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
	}

	/**
	 * Convert signed bytes to a 32-bit unsigned int.
	 */
	private int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
		return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
				+ (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
	}

	/**
	 * Convert signed bytes to a 16-bit short float value.
	 */
	private float bytesToFloat(byte b0, byte b1) {
		int mantissa = unsignedToSigned(unsignedByteToInt(b0)
				+ ((unsignedByteToInt(b1) & 0x0F) << 8), 12);
		int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);
		return (float)(mantissa * Math.pow(10, exponent));
	}

	/**
	 * Convert signed bytes to a 32-bit short float value.
	 */
	private float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
		int mantissa = unsignedToSigned(unsignedByteToInt(b0)
				+ (unsignedByteToInt(b1) << 8)
				+ (unsignedByteToInt(b2) << 16), 24);
		return (float)(mantissa * Math.pow(10, b3));
	}

	/**
	 * Convert an unsigned integer value to a two's-complement encoded
	 * signed value.
	 */
	private int unsignedToSigned(int unsigned, int size) {
		if ((unsigned & (1 << size-1)) != 0) {
			unsigned = -1 * ((1 << size-1) - (unsigned & ((1 << size-1) - 1)));
		}
		return unsigned;
	}
}
