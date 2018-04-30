package no.nordicsemi.android.ble.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataStreamTest {

	@Test
	public void write() {
		final DataStream stream = new DataStream();
		stream.write(new byte[] { 0, 1, 2, 3});
		stream.write(new byte[] { 4, 5, 6});
		assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5, 6}, stream.toByteArray());
	}

	@Test
	public void write_part() {
		final DataStream stream = new DataStream();
		stream.write(new byte[] { 0, 1, 2, 3, 4, 5, 6}, 1, 2);
		assertArrayEquals(new byte[] { 1, 2}, stream.toByteArray());
	}

	@Test
	public void write_data() {
		final DataStream stream = new DataStream();
		final Data data1 = new Data(new byte[] { 0, 2, 4, 6, 8 });
		final Data data2 = new Data(new byte[] { 1, 3, 5, 7, 9 });
		stream.write(data1);
		stream.write(data2);
		assertArrayEquals(new byte[] { 0, 2, 4, 6, 8, 1, 3, 5, 7, 9}, stream.toByteArray());
	}

	@Test
	public void size() {
		final DataStream stream = new DataStream();
		stream.write(new byte[] { 0, 1, 2, 3, 4, 5, 6});
		assertEquals(7, stream.size());
	}

	@Test
	public void toData() {
		final DataStream stream = new DataStream();
		stream.write(new byte[] { 0, 1, 2, 3, 4, 5, 6});
		final Data data = stream.toData();
		assertEquals(0x100, data.getIntValue(Data.FORMAT_UINT16, 0).intValue());
	}
}