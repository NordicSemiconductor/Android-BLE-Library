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

package no.nordicsemi.android.ble.common.callback.cgm;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import org.junit.Test;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class CGMSessionStartTimeDataCallbackTest {
	private boolean success;
	private boolean invalidData;
	private boolean invalidCrc;
	private boolean verified;
	private Calendar result;

	private final DataReceivedCallback callback = new CGMSessionStartTimeDataCallback() {
		@Override
		public void onContinuousGlucoseMonitorSessionStartTimeReceived(@NonNull final BluetoothDevice device, @NonNull final Calendar calendar, final boolean secured) {
			success = true;
			verified = secured;
			result = calendar;
		}

		@Override
		public void onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			invalidCrc = true;
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			invalidData = true;
		}
	};

	@Test
	public void onContinuousGlucoseMonitorSessionStartTimeReceived() {
		final Data data = new Data(new byte[] {(byte) 0xE2, 0x07, 4, 24, 13, 8, 24, 8, 4, (byte) 0xE0, (byte) 0xC2 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertTrue(verified);
		assertEquals(2018, result.get(Calendar.YEAR));
		assertEquals(Calendar.APRIL, result.get(Calendar.MONTH));
		assertEquals(24, result.get(Calendar.DATE));
		assertEquals(13, result.get(Calendar.HOUR_OF_DAY));
		assertEquals(8, result.get(Calendar.MINUTE));
		assertEquals(24, result.get(Calendar.SECOND));
		assertEquals(8 * 60000, result.get(Calendar.ZONE_OFFSET));
		assertEquals(4 * 15 * 60000, result.get(Calendar.DST_OFFSET));
	}

	@Test
	public void onContinuousGlucoseMonitorSessionStartTimeReceived_noYear() {
		final Data data = new Data(new byte[] {(byte) 0, 0, 4, 24, 13, 8, 24, 8, 2 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertFalse(verified);
		assertFalse(result.isSet(Calendar.YEAR));
		assertEquals(Calendar.APRIL, result.get(Calendar.MONTH));
		assertEquals(24, result.get(Calendar.DATE));
		assertEquals(13, result.get(Calendar.HOUR_OF_DAY));
		assertEquals(8, result.get(Calendar.MINUTE));
		assertEquals(24, result.get(Calendar.SECOND));
		assertEquals(8 * 60000, result.get(Calendar.ZONE_OFFSET));
		assertEquals(2 * 15 * 60000, result.get(Calendar.DST_OFFSET));
	}

	@Test
	public void onContinuousGlucoseMonitorSessionStartTimeReceivedWithCrcError() {
		final Data data = new Data(new byte[] {(byte) 0xE2, 0x07, 4, 24, 13, 8, 24, 8, 4, (byte) 0xE0, (byte) 0xC3 });
		callback.onDataReceived(null, data);
		assertTrue(invalidCrc);
	}

	@Test
	public void onInvalidDataReceived() {
		final Data data = new Data(new byte[] {(byte) 0xE2, 0x07, 4, 24, 13, 8, 24, 8, 4, (byte) 0xE0 });
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}
}