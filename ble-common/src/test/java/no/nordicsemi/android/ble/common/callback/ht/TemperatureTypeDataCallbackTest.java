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

import org.junit.Test;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.ht.HealthThermometerTypes;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class TemperatureTypeDataCallbackTest {
	private boolean called;

	@Test
	public void onMeasurementIntervalReceived() {
		final ProfileReadResponse response = new TemperatureTypeDataCallback() {

			@Override
			public void onTemperatureTypeReceived(@NonNull final BluetoothDevice device,
												  final int type) {
				called = true;
				assertEquals("Temperature Type", HealthThermometerTypes.TYPE_EAR, type);
			}
		};

		called = false;
		final Data data = new Data(new byte[] { 3 });
		response.onDataReceived(null, data);
		assertTrue(response.isValid());
		assertTrue(called);
	}

	@Test
	public void onInvalidDataReceived() {
		final ProfileReadResponse response = new TemperatureTypeDataCallback() {
			@Override
			public void onTemperatureTypeReceived(@NonNull final BluetoothDevice device,
												  final int type) {
				called = true;
			}
		};

		called = false;
		final Data data = new Data(new byte[] { 3, 0 });
		response.onDataReceived(null, data);
		assertFalse(called);
		assertFalse(response.isValid());
	}

}