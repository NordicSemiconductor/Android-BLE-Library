package no.nordicsemi.android.ble.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Splits the message into at-most MTU-3 size packets.
 */
public final class DefaultMtuSplitter implements DataSplitter {

	@Nullable
	@Override
	public byte[] chunk(@NonNull final byte[] message, final int index, final int maxLength) {
		final int offset = index * maxLength;
		final int length = Math.min(maxLength, message.length - offset);

		if (length <= 0)
			return null;

		final byte[] data = new byte[length];
		System.arraycopy(message, offset, data, 0, length);
		return data;
	}
}
