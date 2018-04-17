package no.nordicsemi.android.ble.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface DataSplitter {

	/**
	 * The implementation should return a index'th byte array from given message,
	 * with at most maxLength size, or null if no bytes are left to be sent.
	 * @param message the full message to be chunk
	 * @param index index of a packet, 0-based
	 * @param maxLength maximum length of the returned packet. Equals to MTU-3.
	 * @return the packet to be sent, or null if the whole message was already split
	 */
	@Nullable
	byte[] chunk(final @NonNull byte[] message, final int index, final int maxLength);
}
