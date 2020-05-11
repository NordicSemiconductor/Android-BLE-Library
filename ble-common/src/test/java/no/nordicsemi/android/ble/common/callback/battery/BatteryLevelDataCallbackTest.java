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

package no.nordicsemi.android.ble.common.callback.battery;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ConstantConditions")
public class BatteryLevelDataCallbackTest {

	@Test
	public void onBatteryLevelChanged_fullBattery() {
		final DataReceivedCallback callback = new BatteryLevelDataCallback() {
			@Override
			public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
				assertEquals("Correct data", batteryLevel, 100);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final Data data = new Data(new byte[] { 0x64 });
		callback.onDataReceived(null, data);
	}

	@Test
	public void onBatteryLevelChanged_lowBattery() {
		final DataReceivedCallback callback = new BatteryLevelDataCallback() {
			@Override
			public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
				assertEquals("Correct data", batteryLevel, 15);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct data reported as invalid", 1, 2);
			}
		};
		final Data data = new Data(new byte[] { 0x0F });
		callback.onDataReceived(null, data);
	}

	@Test
	public void onInvalidDataReceived_dataTooLong() {
		final DataReceivedCallback callback = new BatteryLevelDataCallback() {
			@Override
			public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
				assertEquals("Invalid date returned Battery Level", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid data", data.size(), 2);
			}
		};
		final Data data = new Data(new byte[] { 0x64, 0x00 });
		callback.onDataReceived(null, data);
	}

	@Test
	public void onInvalidDataReceived_batteryLevelOutOfRange() {
		final DataReceivedCallback callback = new BatteryLevelDataCallback() {
			@Override
			public void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel) {
				assertEquals("Invalid date returned Battery Level", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid data", data.size(), 1);
			}
		};
		final Data data = new Data(new byte[] { 0x65 });
		callback.onDataReceived(null, data);
	}
}