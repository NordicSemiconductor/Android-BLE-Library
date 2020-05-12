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

package no.nordicsemi.android.ble.common.callback.ht;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.ht.TemperatureMeasurementCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class TemperatureMeasurementDataCallbackTest {
	private boolean called;

	@Test
	public void onTemperatureMeasurementReceived() {
		final ProfileReadResponse response = new TemperatureMeasurementDataCallback() {
			@Override
			public void onTemperatureMeasurementReceived(@NonNull final BluetoothDevice device,
														 final float temperature, final int unit,
														 @Nullable final Calendar calendar,
														 @Nullable final Integer type) {
				called = true;
				assertEquals("Temperature", 37.60f, temperature, 0.001f);
				assertEquals("Unit", TemperatureMeasurementCallback.UNIT_C, unit);
				assertNotNull("Calendar present", calendar);
				assertTrue("Year set", calendar.isSet(Calendar.YEAR));
				assertEquals("Year", calendar.get(Calendar.YEAR), 2012);
				assertTrue("Month set", calendar.isSet(Calendar.MONTH));
				assertEquals("Month", calendar.get(Calendar.MONTH), Calendar.DECEMBER);
				assertTrue("Day set", calendar.isSet(Calendar.DATE));
				assertEquals("Day", 5, calendar.get(Calendar.DATE));
				assertEquals("Hour", 11, calendar.get(Calendar.HOUR_OF_DAY));
				assertEquals("Minute", 50, calendar.get(Calendar.MINUTE));
				assertEquals("Seconds", 27, calendar.get(Calendar.SECOND));
				assertEquals("Milliseconds", 0, calendar.get(Calendar.MILLISECOND));
				assertNotNull("Type present", type);
				assertEquals(TemperatureMeasurementCallback.TYPE_FINGER, type.intValue());
			}
		};

		final Data data = new Data(new byte[] { 0x06, (byte) 0xB0, 0x0E, 0x00, (byte) 0xFE, (byte) 0xDC, 0x07, 0x0C, 0x05, 0x0B, 0x32, 0x1B, 0x04 });
		called = false;
		response.onDataReceived(null, data);
		assertTrue(called);
		assertTrue(response.isValid());
	}

	@Test
	public void onInvalidDataReceived() {
		final ProfileReadResponse response = new TemperatureMeasurementDataCallback() {
			@Override
			public void onTemperatureMeasurementReceived(@NonNull final BluetoothDevice device,
														 final float temperature, final int unit,
														 @Nullable final Calendar calendar,
														 @Nullable final Integer type) {
				called = true;
			}
		};

		final Data data = new Data(new byte[] { 0x06, (byte) 0xB0, 0x0E, 0x00, (byte) 0xFE, (byte) 0xDC, 0x07, 0x0C, 0x05, 0x0B, 0x32, 0x1B });
		called = false;
		response.onDataReceived(null, data);
		assertFalse(called);
		assertFalse(response.isValid());
	}
}