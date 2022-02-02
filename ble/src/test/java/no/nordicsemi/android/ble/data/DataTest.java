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

@SuppressWarnings("ConstantConditions")
public class DataTest {

	@Test
	public void setValue_SFLOAT_hex() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(10.1f, Data.FORMAT_SFLOAT, 0);
		assertArrayEquals(new byte[] { 0x65, (byte) 0xF0 }, data.getValue());
	}

	@Test
	public void setValue_SFLOAT_basic() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(1.0f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(1.0f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_roundUp() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(123.45f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(123.5f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_roundDown() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(0.12344f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(0.1234f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_high() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(10000000f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(10000000f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_cutPrecision() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(1000400f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(1000000f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_roundUp_500() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(1000500f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(1001000f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_positiveInfinity() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(Float.POSITIVE_INFINITY, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(Float.POSITIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_negativeInfinity() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(Float.NEGATIVE_INFINITY, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(Float.NEGATIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_nan() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(Float.NaN, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(Float.NaN, value, 0.00);
	}

	@Test
	public void setValue_FLOAT_hex() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(12345.67f, Data.FORMAT_FLOAT, 0);
		assertArrayEquals(new byte[] { (byte) 0x87, (byte) 0xD6, (byte) 0x12, (byte) 0xFE }, data.getValue());
	}

	@Test
	public void setValue_FLOAT_basic() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(1.0f, Data.FORMAT_FLOAT, 0);
		assertArrayEquals(new byte[] { 1, 0, 0, 0 }, data.getValue());
	}

	@Test
	public void setValue_FLOAT_roundUp() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(1234.5678f, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(MutableData.FORMAT_FLOAT, 0);
		assertEquals(1234.568f, value, 0.00001);
	}

	@Test
	public void setValue_FLOAT_positiveInfinity() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(Float.POSITIVE_INFINITY, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(Float.POSITIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_FLOAT_negativeInfinity() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(Float.NEGATIVE_INFINITY, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(Float.NEGATIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_FLOAT_nan() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(Float.NaN, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(Float.NaN, value, 0.00);
	}

	@Test
	public void setValue_UINT8() {
		final MutableData data = new MutableData(new byte[1]);
		data.setValue(200, Data.FORMAT_UINT8, 0);
		assertArrayEquals(new byte[] { (byte) 0xC8 } , data.getValue());
	}

	@Test
	public void getValue_UINT8() {
		final Data data = new Data(new byte[] {(byte) 0xC8 });
		final int value = data.getIntValue(Data.FORMAT_UINT8, 0);
		assertEquals(200, value);
	}

	@Test
	public void setValue_SINT8() {
		final MutableData data = new MutableData(new byte[1]);
		data.setValue(-120, Data.FORMAT_SINT8, 0);
		assertArrayEquals(new byte[] { (byte) 0x88 } , data.getValue());
	}

	@Test
	public void getValue_SINT8() {
		final Data data = new Data(new byte[] { (byte) 0x88 });
		final int value = data.getIntValue(Data.FORMAT_SINT8, 0);
		assertEquals(-120, value);
	}

	@Test
	public void setValue_UINT16() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(26576, Data.FORMAT_UINT16_LE, 0);
		assertArrayEquals(new byte[] { (byte) 0xD0, 0x67 } , data.getValue());
	}

	@Test
	public void setValue_UINT16_BE() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(26576, Data.FORMAT_UINT16_BE, 0);
		assertArrayEquals(new byte[] { 0x67, (byte) 0xD0 } , data.getValue());
	}

	@Test
	public void getValue_UINT16() {
		final Data data = new Data(new byte[] { (byte) 0xD0, 0x67 });
		final int value = data.getIntValue(Data.FORMAT_UINT16_LE, 0);
		assertEquals(26576, value);
	}

	@Test
	public void getValue_UINT16_BE() {
		final Data data = new Data(new byte[] { 0x67, (byte) 0xD0 });
		final int value = data.getIntValue(Data.FORMAT_UINT16_BE, 0);
		assertEquals(26576, value);
	}

	@Test
	public void setValue_SINT16() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(-6192, Data.FORMAT_SINT16_LE, 0);
		assertArrayEquals(new byte[] { (byte) 0xD0, (byte) 0xE7 } , data.getValue());
	}

	@Test
	public void setValue_SINT16_BE() {
		final MutableData data = new MutableData(new byte[2]);
		data.setValue(-6192, Data.FORMAT_SINT16_BE, 0);
		assertArrayEquals(new byte[] { (byte) 0xE7, (byte) 0xD0} , data.getValue());
	}

	@Test
	public void getValue_SINT16() {
		final Data data = new Data(new byte[] { (byte) 0xD0, (byte) 0xE7 });
		final int value = data.getIntValue(Data.FORMAT_SINT16_LE, 0);
		assertEquals(-6192, value);
	}

	@Test
	public void getValue_SINT16_BE() {
		final Data data = new Data(new byte[] { (byte) 0xE7, (byte) 0xD0 });
		final int value = data.getIntValue(Data.FORMAT_SINT16_BE, 0);
		assertEquals(-6192, value);
	}

	@Test
	public void setValue_UINT24() {
		final MutableData data = new MutableData(new byte[3]);
		data.setValue(0x010203, Data.FORMAT_UINT24_LE, 0);
		assertArrayEquals(new byte[] { 0x03, 0x02, 0x01 } , data.getValue());
	}

	@Test
	public void setValue_UINT24_BE() {
		final MutableData data = new MutableData(new byte[3]);
		data.setValue(0x010203, Data.FORMAT_UINT24_BE, 0);
		assertArrayEquals(new byte[] { 0x01, 0x02, 0x03 } , data.getValue());
	}

	@Test
	public void getValue_UINT24() {
		final Data data = new Data(new byte[] { 0x03, 0x02, 0x01 });
		final int value = data.getIntValue(Data.FORMAT_UINT24_LE, 0);
		assertEquals(0x010203, value);
	}

	@Test
	public void getValue_UINT24_BE() {
		final Data data = new Data(new byte[] { 0x01, 0x02, 0x03 });
		final int value = data.getIntValue(Data.FORMAT_UINT24_BE, 0);
		assertEquals(0x010203, value);
	}

	@Test
	public void setValue_SINT24() {
		final MutableData data = new MutableData(new byte[3]);
		data.setValue(0xfefdfd, Data.FORMAT_UINT24_LE, 0);
		assertArrayEquals(new byte[] { (byte) 0xFD, (byte) 0xFD, (byte) 0xFE } , data.getValue());
	}

	@Test
	public void setValue_SINT24_BE() {
		final MutableData data = new MutableData(new byte[3]);
		data.setValue(0xfefdfd, Data.FORMAT_UINT24_BE, 0);
		assertArrayEquals(new byte[] { (byte) 0xFE, (byte) 0xFD, (byte) 0xFD } , data.getValue());
	}

	@Test
	public void getValue_SINT24() {
		final MutableData data = new MutableData(new byte[] { (byte) 0xFD, (byte) 0xFD, (byte) 0xFE });
		final int value = data.getIntValue(Data.FORMAT_UINT24_LE, 0);
		assertEquals(0xfefdfd, value);
	}

	@Test
	public void getValue_SINT24_BE() {
		final MutableData data = new MutableData(new byte[] { (byte) 0xFE, (byte) 0xFD, (byte) 0xFD });
		final int value = data.getIntValue(Data.FORMAT_UINT24_BE, 0);
		assertEquals(0xfefdfd, value);
	}

	@Test
	public void setValue_UINT32() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(0x01020304, Data.FORMAT_UINT32_LE, 0);
		assertArrayEquals(new byte[] { 0x04, 0x03, 0x02, 0x01 } , data.getValue());
	}

	@Test
	public void setValue_UINT32_BE() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(0x04030201, Data.FORMAT_UINT32_BE, 0);
		assertArrayEquals(new byte[] { 0x04, 0x03, 0x02, 0x01 } , data.getValue());
	}

	@Test
	public void getValue_UINT32() {
		final Data data = new Data(new byte[] { 0x04, 0x03, 0x02, 0x01 });
		final int value = data.getIntValue(Data.FORMAT_UINT32_LE, 0);
		assertEquals(0x01020304, value);
	}

	@Test
	public void getValue_UINT32_BE() {
		final Data data = new Data(new byte[] { 0x04, 0x03, 0x02, 0x01 });
		final int value = data.getIntValue(Data.FORMAT_UINT32_BE, 0);
		assertEquals(0x04030201, value);
	}

	@Test
	public void setValue_SINT32() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(0xfefdfd00, Data.FORMAT_UINT32_LE, 0);
		assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0xFD, (byte) 0xFD, (byte) 0xFE } , data.getValue());
	}

	@Test
	public void setValue_SINT32_BE() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(0x00fdfdfe, Data.FORMAT_UINT32_BE, 0);
		assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0xFD, (byte) 0xFD, (byte) 0xFE } , data.getValue());
	}

	@Test
	public void getValue_SINT32() {
		final Data data = new Data(new byte[] { (byte) 0x00, (byte) 0xFD, (byte) 0xFD, (byte) 0xFE });
		final int value = data.getIntValue(Data.FORMAT_UINT32_LE, 0);
		assertEquals(0xfefdfd00, value);
	}

	@Test
	public void getValue_SINT32_BE() {
		final Data data = new Data(new byte[] { (byte) 0x00, (byte) 0xFD, (byte) 0xFD, (byte) 0xFE });
		final int value = data.getIntValue(Data.FORMAT_UINT32_BE, 0);
		assertEquals(0x00fdfdfe, value);
	}

	@Test
	public void setValue_UINT32_big() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(0xF0000001L, Data.FORMAT_UINT32_LE, 0);
		assertArrayEquals(new byte[] { 0x01, 0x00, 0x00, (byte) 0xF0 } , data.getValue());
	}

	@Test
	public void setValue_UINT32_BE_big() {
		final MutableData data = new MutableData(new byte[4]);
		data.setValue(0xF0000001L, Data.FORMAT_UINT32_BE, 0);
		assertArrayEquals(new byte[] { (byte) 0xF0, 0x00, 0x00, 0x01 } , data.getValue());
	}

	@Test
	public void getValue_UINT32_big() {
		final Data data = new Data(new byte[] { 0x01, 0x00, 0x00, (byte) 0xF0 });
		final long value = data.getLongValue(Data.FORMAT_UINT32_LE, 0);
		assertEquals(0xF0000001L, value);
	}

	@Test
	public void getValue_UINT32_BE_big() {
		final Data data = new Data(new byte[] { (byte) 0xF0, 0x00, 0x00, 0x01 });
		final long value = data.getLongValue(Data.FORMAT_UINT32_BE, 0);
		assertEquals(0xF0000001L, value);
	}
}