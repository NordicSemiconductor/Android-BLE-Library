package no.nordicsemi.android.ble.data;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public class Data implements Parcelable {
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

	protected byte[] mValue;

	public Data() {
		this.mValue = null;
	}

	public Data(final byte[] value) {
		this.mValue = value;
	}

	public static Data from(final BluetoothGattCharacteristic characteristic) {
		return new Data(characteristic.getValue());
	}

	public static Data from(final BluetoothGattDescriptor descriptor) {
		return new Data(descriptor.getValue());
	}

	public static Data opCode(final byte opCode) {
		return new Data(new byte[] { opCode });
	}

	public static Data opCode(final byte opCode, final byte parameter) {
		return new Data(new byte[] { opCode, parameter });
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
	 * Returns a long value from the byte array.
	 * <p>Only {@link #FORMAT_UINT32} and {@link #FORMAT_SINT32} are supported.
	 * <p>The formatType parameter determines how the value
	 * is to be interpreted. For example, setting formatType to
	 * {@link #FORMAT_UINT32} specifies that the first four bytes of the
	 * value at the given offset are interpreted to generate the
	 * return value.
	 *
	 * @param formatType The format type used to interpret the value.
	 * @param offset     Offset at which the integer value can be found.
	 * @return Cached value or null of offset exceeds value size.
	 */
	public Long getLongValue(final int formatType, final int offset) {
		if ((offset + getTypeLen(formatType)) > size()) return null;

		switch (formatType) {
			case FORMAT_SINT32:
				return unsignedToSigned(unsignedBytesToLong(mValue[offset],
						mValue[offset + 1], mValue[offset + 2], mValue[offset + 3]), 32);

			case FORMAT_UINT32:
				return unsignedBytesToLong(mValue[offset], mValue[offset + 1],
						mValue[offset + 2], mValue[offset + 3]);
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
	 * Returns the size of a give value type.
	 */
	public static int getTypeLen(final int formatType) {
		return formatType & 0xF;
	}

	/**
	 * Convert a signed byte to an unsigned int.
	 */
	private static int unsignedByteToInt(final byte b) {
		return b & 0xFF;
	}

	/**
	 * Convert a signed byte to an unsigned int.
	 */
	private static long unsignedByteToLong(final byte b) {
		return b & 0xFFL;
	}

	/**
	 * Convert signed bytes to a 16-bit unsigned int.
	 */
	private static int unsignedBytesToInt(final byte b0, final byte b1) {
		return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
	}

	/**
	 * Convert signed bytes to a 32-bit unsigned int.
	 */
	private static int unsignedBytesToInt(final byte b0, final byte b1, final byte b2, final byte b3) {
		return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
				+ (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
	}

	/**
	 * Convert signed bytes to a 32-bit unsigned long.
	 */
	private static long unsignedBytesToLong(final byte b0, final byte b1, final byte b2, final byte b3) {
		return (unsignedByteToLong(b0) + (unsignedByteToLong(b1) << 8))
				+ (unsignedByteToLong(b2) << 16) + (unsignedByteToLong(b3) << 24);
	}

	/**
	 * Convert signed bytes to a 16-bit short float value.
	 */
	private static float bytesToFloat(final byte b0, final byte b1) {
		int mantissa = unsignedToSigned(unsignedByteToInt(b0)
				+ ((unsignedByteToInt(b1) & 0x0F) << 8), 12);
		int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);
		return (float) (mantissa * Math.pow(10, exponent));
	}

	/**
	 * Convert signed bytes to a 32-bit short float value.
	 */
	private static float bytesToFloat(final byte b0, final byte b1, final byte b2, final byte b3) {
		int mantissa = unsignedToSigned(unsignedByteToInt(b0)
				+ (unsignedByteToInt(b1) << 8)
				+ (unsignedByteToInt(b2) << 16), 24);
		return (float) (mantissa * Math.pow(10, b3));
	}

	/**
	 * Convert an unsigned integer value to a two's-complement encoded
	 * signed value.
	 */
	private static int unsignedToSigned(int unsigned, final int size) {
		if ((unsigned & (1 << size - 1)) != 0) {
			unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
		}
		return unsigned;
	}

	/**
	 * Convert an unsigned long value to a two's-complement encoded
	 * signed value.
	 */
	private static long unsignedToSigned(long unsigned, final int size) {
		if ((unsigned & (1 << size - 1)) != 0) {
			unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
		}
		return unsigned;
	}

	// Parcelable
	protected Data(final Parcel in) {
		mValue = in.createByteArray();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeByteArray(mValue);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<Data> CREATOR = new Creator<Data>() {
		@Override
		public Data createFromParcel(final Parcel in) {
			return new Data(in);
		}

		@Override
		public Data[] newArray(final int size) {
			return new Data[size];
		}
	};
}
