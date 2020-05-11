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

package no.nordicsemi.android.ble.common.callback.sc;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.sc.SensorLocationCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class SensorLocationDataCallbackTest {
	private boolean called;

	@Test
	public void onSensorLocationReceived() {
		final ProfileReadResponse callback = new SensorLocationDataCallback() {
			@Override
			public void onSensorLocationReceived(@NonNull final BluetoothDevice device, final int location) {
				called = true;
				assertEquals("Location", SensorLocationCallback.SENSOR_LOCATION_REAR_WHEEL, location);
			}
		};

		called = false;
		final Data data = new Data(new byte[] { 12 });
		callback.onDataReceived(null, data);
		assertTrue(called);
		assertTrue(callback.isValid());
	}

	@Test
	public void onInvalidDataReceived() {
		final ProfileReadResponse callback = new SensorLocationDataCallback() {
			@Override
			public void onSensorLocationReceived(@NonNull final BluetoothDevice device, final int location) {
				called = true;
			}
		};

		called = false;
		final Data data = new Data(new byte[] { 0x01, 0x02 });
		callback.onDataReceived(null, data);
		assertFalse(called);
		assertFalse(callback.isValid());
	}
}