package no.nordicsemi.android.ble.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class DefaultMtuSplitter implements ValueSplitter {

	@Nullable
	@Override
	public byte[] chunk(@NonNull final byte[] message, final int count, final int maxLength) {
		final int offset = count * maxLength;
		final int length = Math.min(maxLength, message.length - offset);

		if (length <= 0)
			return null;

		final byte[] data = new byte[length];
		System.arraycopy(message, offset, data, 0, length);
		return data;
	}
}
