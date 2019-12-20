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

}
