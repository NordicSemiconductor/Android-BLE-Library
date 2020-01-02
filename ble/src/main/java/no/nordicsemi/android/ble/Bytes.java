package no.nordicsemi.android.ble;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

final class Bytes {

	/**
	 * Copies max length bytes of the given array, starting from offset.
	 * @param value the data buffer.
	 * @param offset the initial offset.
	 * @param length maximum length/
	 * @return The copy.
	 */
	static byte[] copy(@Nullable final byte[] value,
					   @IntRange(from = 0) final int offset,
					   @IntRange(from = 0) final int length) {
		if (value == null || offset > value.length)
			return null;
		final int maxLength = Math.min(value.length - offset, length);
		final byte[] copy = new byte[maxLength];
		System.arraycopy(value, offset, copy, 0, maxLength);
		return copy;
	}

	/**
	 * Concatenates two byte arrays. The right one will be places at the given offset.
	 * @param left the first part
	 * @param right the second part
	 * @param offset the offset
	 * @return the concatenated byte array. Result will be at least offset-bytes long.
	 */
	static byte[] concat(@Nullable final byte[] left, @Nullable final byte[] right, @IntRange(from = 0) final int offset) {
		final int length = offset + (right != null ? right.length : 0);
		final byte[] result = new byte[length];
		if (left != null)
			System.arraycopy(left, 0, result, 0, left.length);
		if (right != null)
			System.arraycopy(right, 0, result, offset, right.length);
		return result;
	}

}
