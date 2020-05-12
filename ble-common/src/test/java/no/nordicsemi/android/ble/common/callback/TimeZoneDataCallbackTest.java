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

package no.nordicsemi.android.ble.common.callback;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class TimeZoneDataCallbackTest {
	private int result;
	private boolean success;
	private boolean unknownTimeZone;
	private boolean invalidData;

	private final DataReceivedCallback callback = new TimeZoneDataCallback() {
		@Override
		public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			success = false;
			unknownTimeZone = false;
			invalidData = false;
			result = Integer.MAX_VALUE;
			super.onDataReceived(device, data);
		}

		@Override
		public void onTimeZoneReceived(@NonNull final BluetoothDevice device, final int offset) {
			success = true;
			result = offset;
		}

		@Override
		public void onUnknownTimeZoneReceived(@NonNull final BluetoothDevice device) {
			unknownTimeZone = true;
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			invalidData = true;
		}
	};

	@Test
	public void onTimeZoneReceived_basic() {
		final Data data = new Data(new byte[] { 1 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertEquals(15, result);
	}

	@Test
	public void onTimeZoneReceived_unknown() {
		final Data data = new Data(new byte[] { -128 });
		callback.onDataReceived(null, data);
		assertTrue(unknownTimeZone);
	}

	@Test
	public void onTimeZoneReceived_invalid() {
		final Data data = new Data(new byte[] { 60 });
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}
}