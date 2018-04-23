package no.nordicsemi.android.ble.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataTest {

	@Test
	public void setValue_SFLOAT_basic() {
		final Data data = new Data(new byte[2]);
		data.setValue(1.0f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(1.0f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_roundUp() {
		final Data data = new Data(new byte[2]);
		data.setValue(123.45f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(123.5f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_roundDown() {
		final Data data = new Data(new byte[2]);
		data.setValue(0.12344f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(0.1234f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_high() {
		final Data data = new Data(new byte[2]);
		data.setValue(10000000f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(10000000f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_cutPrecision() {
		final Data data = new Data(new byte[2]);
		data.setValue(1000400f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(1000000f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_roundUp_500() {
		final Data data = new Data(new byte[2]);
		data.setValue(1000500f, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(1001000f, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_positiveInfinity() {
		final Data data = new Data(new byte[2]);
		data.setValue(Float.POSITIVE_INFINITY, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(Float.POSITIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_negativeInfinity() {
		final Data data = new Data(new byte[2]);
		data.setValue(Float.NEGATIVE_INFINITY, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(Float.NEGATIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_SFLOAT_nan() {
		final Data data = new Data(new byte[2]);
		data.setValue(Float.NaN, Data.FORMAT_SFLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_SFLOAT, 0);
		assertEquals(Float.NaN, value, 0.00);
	}

	@Test
	public void setValue_FLOAT_basic() {
		final Data data = new Data(new byte[4]);
		data.setValue(1.0f, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(1.0f, value, 0.00);
	}

	@Test
	public void setValue_FLOAT_roundUp() {
		final Data data = new Data(new byte[4]);
		data.setValue(1234.5678f, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(1234.568f, value, 0.00001);
	}

	@Test
	public void setValue_FLOAT_positiveInfinity() {
		final Data data = new Data(new byte[4]);
		data.setValue(Float.POSITIVE_INFINITY, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(Float.POSITIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_FLOAT_negativeInfinity() {
		final Data data = new Data(new byte[4]);
		data.setValue(Float.NEGATIVE_INFINITY, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(Float.NEGATIVE_INFINITY, value, 0.00);
	}

	@Test
	public void setValue_FLOAT_nan() {
		final Data data = new Data(new byte[4]);
		data.setValue(Float.NaN, Data.FORMAT_FLOAT, 0);
		final float value = data.getFloatValue(Data.FORMAT_FLOAT, 0);
		assertEquals(Float.NaN, value, 0.00);
	}
}