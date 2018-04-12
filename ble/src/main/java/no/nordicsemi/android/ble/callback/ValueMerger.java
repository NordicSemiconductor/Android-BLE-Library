package no.nordicsemi.android.ble.callback;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

public interface ValueMerger {

	/**
	 * This method should merge the last packet into the message
	 * @param message the full message, initially empty. The last packet needs to be put into the message
	 * @param lastPacket the data received in the last read/notify/indicate operation
	 * @param count an index of the packet, 0-based (if you expect 3 packets, they will be called with indexes 0, 1, 2)
	 * @return true if the message is complete, false if more data are expected
	 */
	boolean merge(final @NonNull ByteBuffer message, final @NonNull byte[] lastPacket, final int count);
}
