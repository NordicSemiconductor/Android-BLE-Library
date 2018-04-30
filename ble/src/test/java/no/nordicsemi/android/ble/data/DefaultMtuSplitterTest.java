package no.nordicsemi.android.ble.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultMtuSplitterTest {
	private final String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
			"tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
			"nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis " +
			"aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat " +
			"nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui " +
			"officia deserunt mollit anim id est laborum.";

	@Test
	public void chunk_23() {
		final int MTU = 23;
		final DefaultMtuSplitter splitter = new DefaultMtuSplitter();
		final byte[] result = splitter.chunk(text.getBytes(), 1, MTU - 3);
		assertArrayEquals(text.substring(MTU - 3, 2 * (MTU - 3)).getBytes(), result);
	}

	@Test
	public void chunk_43() {
		final int MTU = 43;
		final DefaultMtuSplitter splitter = new DefaultMtuSplitter();
		final byte[] result = splitter.chunk(text.getBytes(), 2, MTU - 3);
		assertArrayEquals(text.substring(2 * (MTU - 3), 3 * (MTU - 3)).getBytes(), result);
	}

	@Test
	public void chunk_end() {
		final int MTU = 23;
		final DefaultMtuSplitter splitter = new DefaultMtuSplitter();
		final byte[] result = splitter.chunk(text.getBytes(), 200, MTU - 3);
		assertNull(result);
	}
}