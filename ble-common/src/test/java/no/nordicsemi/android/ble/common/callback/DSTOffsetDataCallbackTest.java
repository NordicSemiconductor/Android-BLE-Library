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
import no.nordicsemi.android.ble.common.profile.DSTOffsetCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
public class DSTOffsetDataCallbackTest {
	private DSTOffsetCallback.DSTOffset result;
	private boolean success;
	private boolean invalidData;

	private final DataReceivedCallback callback = new DSTOffsetDataCallback() {
		@Override
		public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			success = false;
			invalidData = false;
			result = null;
			super.onDataReceived(device, data);
		}

		@Override
		public void onDSTOffsetReceived(@NonNull final BluetoothDevice device, @NonNull final DSTOffset offset) {
			success = true;
			result = offset;
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			invalidData = true;
		}
	};

	@Test
	public void onDSTOffsetReceived_standard() {
		final Data data = new Data(new byte[] { 0 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertSame(DSTOffsetCallback.DSTOffset.STANDARD_TIME, result);
	}

	@Test
	public void onDSTOffsetReceived_half() {
		final Data data = new Data(new byte[] { 2 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertSame(DSTOffsetCallback.DSTOffset.HALF_AN_HOUR_DAYLIGHT_TIME, result);
	}

	@Test
	public void onDSTOffsetReceived_daylight() {
		final Data data = new Data(new byte[] { 4 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertSame(DSTOffsetCallback.DSTOffset.DAYLIGHT_TIME, result);
	}

	@Test
	public void onDSTOffsetReceived_double() {
		final Data data = new Data(new byte[] { 8 });
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertSame(DSTOffsetCallback.DSTOffset.DOUBLE_DAYLIGHT_TIME, result);
	}

	@Test
	public void onDSTOffsetReceived_unknown() {
		final Data data = new Data(new byte[] {(byte) 255});
		callback.onDataReceived(null, data);
		assertTrue(success);
		assertSame(DSTOffsetCallback.DSTOffset.UNKNOWN, result);
	}

	@Test
	public void onDSTOffsetReceived_invalid() {
		final Data data = new Data(new byte[] { 17 });
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}

	@Test
	public void onDSTOffsetReceived_tooShort() {
		final Data data = new Data(new byte[0]);
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}
}