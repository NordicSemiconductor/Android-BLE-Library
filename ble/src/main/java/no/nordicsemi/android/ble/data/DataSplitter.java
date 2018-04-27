package no.nordicsemi.android.ble.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface DataSplitter {

	/**
	 * The implementation should return a index'th byte array from given message,
	 * with at most maxLength size, or null if no bytes are left to be sent.
	 *
	 * @param message the full message to be chunk.
	 * @param index index of a packet, 0-based.
	 * @param maxLength maximum length of the returned packet. Equals to MTU-3.
	 *                  Use {@link no.nordicsemi.android.ble.BleManager#requestMtu(int)} to request
	 *                  higher MTU, or {@link no.nordicsemi.android.ble.BleManager#overrideMtu(int)}
	 *                  if the MTU change was initiated by the target device.
	 * @return the packet to be sent, or null if the whole message was already split.
	 */
	@Nullable
	byte[] chunk(final @NonNull byte[] message, final int index, final int maxLength);
}
