package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class RequestTest {
	private final String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
			"tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
			"nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis " +
			"aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat " +
			"nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui " +
			"officia deserunt mollit anim id est laborum.";
	private final int MTU = 23;

	private BluetoothGattCharacteristic characteristic;
	private byte[] chunk;
	private boolean called;
	private boolean done;

	@Before
	public void init() {
		characteristic = new BluetoothGattCharacteristic(UUID.randomUUID(), BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
	}

	@Test
	public void split_basic() {
		final WriteRequest request = Request.newWriteRequest(characteristic, text.getBytes(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
				.split();
		chunk = request.getData(MTU);

		// Verify the chunk
		assertNotNull(chunk);
		assertEquals(MTU - 3, chunk.length);
		final String expected = text.substring(0, MTU - 3);
		assertArrayEquals(expected.getBytes(), chunk);
	}

	@Test
	public void split_highMtu() {
		final int MTU_HIGH = 276;

		final WriteRequest request = Request.newWriteRequest(characteristic, text.getBytes(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
				.split();
		chunk = request.getData(MTU_HIGH);
		// Verify the chunk
		assertNotNull(chunk);
		assertEquals(MTU_HIGH - 3, chunk.length);
		final String expected = text.substring(0, MTU_HIGH - 3);
		assertArrayEquals(expected.getBytes(), chunk);
	}

	@Test
	public void split_callbacks() {
		// Create a WriteRequest with the default splitter and custom progress and success callbacks
		final WriteRequest request = Request.newWriteRequest(characteristic, text.getBytes(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
				.split((device, data, index) -> {
					// Validate the progress callback
					called = true;
					assertNotNull(data);
					assertArrayEquals(chunk, data);
					assertTrue(data.length <= MTU - 3);
				}).done(device -> done = true);

		done = false;
		do {
			// Get next packet until null is returned
			called = false;
			chunk = request.getData(MTU);
			if (chunk != null) {
				// If there are bytes to send, let's assume they were sent:
				// Sending data...
				// Data sent

				// Notify request that the packet was sent. This will increment count value
				request.notifyPacketSent(null, chunk);
				assertTrue(called);
			} else {
				// Progress callback shouldn't be called without calling notifyPacketSent(...)
				assertFalse(called);
			}
		} while (request.hasMore());

		// When there is nothing more to be sent notify about success
		request.notifySuccess(null);

		// Check if success callback was called
		assertTrue(done);
	}

	@Test
	public void split_merge() {
		// The WriteRequest is only to split the text into chunks
		final WriteRequest request = Request.newWriteRequest(characteristic, text.getBytes(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
				.split();

		// Create ReadRequest that will merge packets until the complete text is in the stream
		final ReadRequest readRequest = Request.newReadRequest(characteristic)
				.merge((output, lastPacket, index) -> {
					// Simply copy all bytes from the lastPacket to the stream
					output.write(lastPacket);
					return output.size() == text.length();
				}, (device, data, index) -> {
					// Validate the progress callback
					called = true;
					assertNotNull(data);
					assertArrayEquals(chunk, data);
					assertTrue(data.length <= MTU - 3);
				})
				.with((device, data) -> {
					// This should contain the whole data
					done = true;
					assertArrayEquals(text.getBytes(), data.getValue());
				})
				.done(device -> done = true);

		done = false;
		do {
			// Get next packet until null is returned
			called = false;
			chunk = request.getData(MTU);
			if (chunk != null) {
				// If there are bytes to send, let's assume they were sent:
				// Sending data...
				// Data sent

				// Notify request that the packet was sent. This will increment count value
				request.notifyPacketSent(null, chunk);

				// Having the chunk, let's pretend we just read it
				called = false;
				readRequest.notifyValueChanged(null, chunk);
				// Check if the progress callback was called with correct chunk
				assertTrue(called);
			}
		} while (request.hasMore());
		// Verify that the data callback was called
		assertTrue(done);

		// Verify that the success callback is called
		called = false;
		done = false;
		readRequest.notifySuccess(null);
		assertTrue(done);
	}
}