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

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class CGMSessionRunTimeDataCallbackTest {
	private boolean called;

	@Test
	public void onContinuousGlucoseMonitorSessionRunTimeReceived_withCrc() {
		final DataReceivedCallback callback = new CGMSessionRunTimeDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceived(@NonNull final BluetoothDevice device, final int sessionRunTime, final boolean secured) {
				called = true;
				assertEquals("Session Run Time", 2, sessionRunTime);
				assertTrue(secured);
			}

			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid CRC reported", 1, 2);
			}


			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid data reported", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[4]);
		assertTrue(data.setValue(2, Data.FORMAT_UINT16, 0));
		assertTrue(data.setValue(0xC308, Data.FORMAT_UINT16, 2));
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onContinuousGlucoseMonitorSessionRunTimeReceived_noCrc() {
		final DataReceivedCallback callback = new CGMSessionRunTimeDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceived(@NonNull final BluetoothDevice device, final int sessionRunTime, final boolean secured) {
				called = true;
				assertEquals("Session Run Time", 2, sessionRunTime);
				assertFalse(secured);
			}

			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid CRC reported", 1, 2);
			}


			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid data reported", 1, 2);
			}
		};
		final Data data = new Data(new byte[] { 2, 0 });
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onContinuousGlucoseMonitorSessionRunTimeReceivedWithCrcError() {
		final DataReceivedCallback callback = new CGMSessionRunTimeDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceived(@NonNull final BluetoothDevice device, final int sessionRunTime, final boolean secured) {
				assertEquals("Invalid CRC but correct packet reported", 1, 2);
			}

			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				called = true;
			}


			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Correct packet but invalid data reported", 1, 2);
			}
		};
		final MutableData data = new MutableData(new byte[4]);
		assertTrue(data.setValue(2, Data.FORMAT_UINT16, 0));
		assertTrue(data.setValue(0xC309, Data.FORMAT_UINT16, 2));
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}

	@Test
	public void onInvalidDataReceived() {
		final DataReceivedCallback callback = new CGMSessionRunTimeDataCallback() {
			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceived(@NonNull final BluetoothDevice device, final int sessionRunTime, final boolean secured) {
				assertEquals("Invalid packet but correct packet reported", 1, 2);
			}

			@Override
			public void onContinuousGlucoseMonitorSessionRunTimeReceivedWithCrcError(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				assertEquals("Invalid packet but invalid CRC reported", 1, 2);
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				called = true;
			}
		};
		final MutableData data = new MutableData(new byte[3]);
		assertTrue(data.setValue(2, Data.FORMAT_UINT16, 0));
		assertTrue(data.setValue(1, Data.FORMAT_UINT8, 2));
		called = false;
		callback.onDataReceived(null, data);
		assertTrue(called);
	}
}