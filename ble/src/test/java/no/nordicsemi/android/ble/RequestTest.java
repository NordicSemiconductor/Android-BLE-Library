/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import androidx.annotation.NonNull;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class RequestTest {
	private final static class SynchronousHandler implements CallbackHandler {
		@Override
		public void post(@NonNull final Runnable r) {
			r.run();
		}

		@Override
		public void postDelayed(@NonNull final Runnable r, final long delayMillis) {
			r.run();
		}

		@Override
		public void removeCallbacks(@NonNull final Runnable r) {
			// do nothing
		}
	}

	private final String text =
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod " +
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
		request.handler = new SynchronousHandler();

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
		request.handler = new SynchronousHandler();

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
		readRequest.handler = new SynchronousHandler();

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