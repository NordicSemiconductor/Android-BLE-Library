package no.nordicsemi.android.ble.data;

import java.io.ByteArrayOutputStream;

public class DataStream {
	private ByteArrayOutputStream buffer;

	public DataStream() {
		buffer = new ByteArrayOutputStream();
	}

	public boolean write(final byte[] data) {
		if (data == null)
			return false;

		return write(data, 0, data.length);
	}

	public boolean write(final byte[] data, final int offset, final int length) {
		if (data == null || data.length < offset)
			return false;

		final int len = Math.min(data.length - offset, length);
		buffer.write(data, offset, len);
		return true;
	}

	public boolean write(final Data data) {
		return data != null && write(data.getValue());
	}

	public int size() {
		return buffer.size();
	}

	public byte[] toByteArray() {
		return buffer.toByteArray();
	}

	public Data toData() {
		return new Data(buffer.toByteArray());
	}
}
