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

package no.nordicsemi.android.ble.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class CRC16Test {

	@Test
	public void CCITT_Kermit_A() {
		final byte[] data = new byte[] { 'A' };
		assertEquals(0x538D, CRC16.CCITT_Kermit(data, 0, 1));
	}

	@Test
	public void CCITT_Kermit_123456789() {
		final byte[] data = "123456789".getBytes();
		assertEquals(0x2189, CRC16.CCITT_Kermit(data, 0, 9));
	}

	@Test
	public void CCITT_Kermit_123456789_offset() {
		final byte[] data = "SPACE123456789".getBytes();
		assertEquals(0x2189, CRC16.CCITT_Kermit(data, 5, 9));
	}

	@Test
	public void CCITT_Kermit_empty() {
		final byte[] data = new byte[0];
		assertEquals(0x0000, CRC16.CCITT_Kermit(data, 0, 1));
	}

	@Test
	public void CCITT_FALSE_A() {
		final byte[] data = new byte[] { 'A' };
		assertEquals(0xB915, CRC16.CCITT_FALSE(data, 0, 1));
	}

	@Test
	public void CCITT_FALSE_123456789() {
		final byte[] data = "123456789".getBytes();
		assertEquals(0x29B1, CRC16.CCITT_FALSE(data, 0, 9));
	}

	@Test
	public void CCITT_FALSE_A_offset() {
		final byte[] data = "1234567A89".getBytes();
		assertEquals(0xB915, CRC16.CCITT_FALSE(data, 7, 1));
	}

	@Test
	public void CCITT_FALSE_empty() {
		final byte[] data = new byte[0];
		assertEquals(0xFFFF, CRC16.CCITT_FALSE(data, 0, 0));
	}

	@Test
	public void MCRF4XX_A() {
		final byte[] data = new byte[] { 'A' };
		assertEquals(0x5C0A, CRC16.MCRF4XX(data, 0, 1));
	}

	@Test
	// See: http://ww1.microchip.com/downloads/en/AppNotes/00752a.pdf
	public void MCRF4XX_8552F189() {
		final byte[] data = new byte[] {(byte) 0x58, (byte) 0x25, (byte) 0x1F, (byte) 0x98};
		assertEquals(0x07F1, CRC16.MCRF4XX(data, 0, 4));
	}

	@Test
	public void MCRF4XX_123456789() {
		final byte[] data = "123456789".getBytes();
		assertEquals(0x6F91, CRC16.MCRF4XX(data, 0, 9));
	}

	@Test
	// See: https://www.bluetooth.com/specifications/gatt -> CGMS 1.0.1 -> 3.11 CRC Calculation
	public void MCRF4XX_CGMS() {
		final byte[] data = new byte[] { 0x3E, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 };
		// In the CGMS 1.0.1 PDF the result CRC is inverted: 0x012F
		assertEquals(0x2F01, CRC16.MCRF4XX(data, 0, 10));
	}

	@Test
	public void AUG_CCITT_A() {
		final byte[] data = new byte[] { 'A' };
		assertEquals(0x9479, CRC16.AUG_CCITT(data, 0, 1));
	}

	@Test
	public void AUG_CCITT_123456789() {
		final byte[] data = "123456789".getBytes();
		assertEquals(0xE5CC, CRC16.AUG_CCITT(data, 0, 9));
	}

	@Test
	public void AUG_CCITT_empty() {
		final byte[] data = new byte[0];
		assertEquals(0x1D0F, CRC16.AUG_CCITT(data, 0, 0));
	}

	@Test
	public void ARC_A() {
		final byte[] data = new byte[] { 'A' };
		assertEquals(0x30C0, CRC16.ARC(data, 0, 1));
	}

	@Test
	public void ARC_123456789() {
		final byte[] data = "123456789".getBytes();
		assertEquals(0xBB3D, CRC16.ARC(data, 0, 9));
	}

	@Test
	public void ARC_empty() {
		final byte[] data = new byte[0];
		assertEquals(0x0000, CRC16.ARC(data, 0, 0));
	}

	@Test
	public void MAXIM_A() {
		final byte[] data = new byte[] { 'A' };
		assertEquals(0xCF3F, CRC16.MAXIM(data, 0, 1));
	}

	@Test
	public void MAXIM_123456789() {
		final byte[] data = "123456789".getBytes();
		assertEquals(0x44C2, CRC16.MAXIM(data, 0, 9));
	}

	@Test
	public void MAXIM_empty() {
		final byte[] data = new byte[0];
		assertEquals(0xFFFF, CRC16.MAXIM(data, 0, 0));
	}
}