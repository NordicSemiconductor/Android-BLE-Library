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

package no.nordicsemi.android.ble.data;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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

	@SuppressWarnings("ConstantConditions")
	@Test
	public void toData() {
		final DataStream stream = new DataStream();
		stream.write(new byte[] { 0, 1, 2, 3, 4, 5, 6});
		final Data data = stream.toData();
		assertEquals(0x100, data.getIntValue(Data.FORMAT_UINT16_LE, 0).intValue());
	}
}