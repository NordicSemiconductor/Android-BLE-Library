package no.nordicsemi.android.ble.callback;

import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;

public interface ValueMerger {

	/**
	 * This method should merge the last packet into the output message.
	 * @param output the stream for the output message, initially empty
	 * @param lastPacket the data received in the last read/notify/indicate operation
	 * @param count an index of the packet, 0-based (if you expect 3 packets, they will be called with indexes 0, 1, 2)
	 * @return true if the message is complete, false if more data are expected
	 */
	boolean merge(final @NonNull ByteArrayOutputStream output, final @NonNull byte[] lastPacket, final int count);
}
