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
import no.nordicsemi.android.ble.data.MutableData;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class SpeedAndCadenceControlPointDataCallbackTest {
	private boolean success;
	private int requestCode;
	private int errorCode;
	private int[] locations;

	private final ProfileReadResponse response = new SpeedAndCadenceControlPointDataCallback() {
		@Override
		public void onSCOperationCompleted(@NonNull final BluetoothDevice device, final int requestCode) {
			SpeedAndCadenceControlPointDataCallbackTest.this.success = true;
			SpeedAndCadenceControlPointDataCallbackTest.this.requestCode = requestCode;
		}

		@Override
		public void onSCOperationError(@NonNull final BluetoothDevice device, final int requestCode, final int errorCode) {
			SpeedAndCadenceControlPointDataCallbackTest.this.success = false;
			SpeedAndCadenceControlPointDataCallbackTest.this.errorCode = errorCode;
			SpeedAndCadenceControlPointDataCallbackTest.this.requestCode = requestCode;
		}

		@Override
		public void onSupportedSensorLocationsReceived(@NonNull final BluetoothDevice device, @NonNull final int[] locations) {
			SpeedAndCadenceControlPointDataCallbackTest.this.success = true;
			SpeedAndCadenceControlPointDataCallbackTest.this.requestCode = SC_OP_CODE_REQUEST_SUPPORTED_SENSOR_LOCATIONS;
			SpeedAndCadenceControlPointDataCallbackTest.this.locations = locations;
		}
	};

	@Test
	public void onSCOperationCompleted() {
		final MutableData data = new MutableData(new byte[] { 0x10, 0x01, 0x01});
		response.onDataReceived(null, data);
		assertTrue(success);
		assertEquals(0, errorCode);
		assertEquals(1, requestCode);
		assertNull(locations);
	}

	@Test
	public void onSCOperationError() {
		final MutableData data = new MutableData(new byte[] { 0x10, 0x02, 0x02});
		response.onDataReceived(null, data);
		assertFalse(success);
		assertEquals(2, errorCode);
		assertEquals(2, requestCode);
		assertNull(locations);
	}

	@Test
	public void onSupportedSensorLocationsReceived() {
		final MutableData data = new MutableData(new byte[] { 0x10, 0x04, 0x01, 1, 2, 3});
		response.onDataReceived(null, data);
		assertTrue(success);
		assertEquals(0, errorCode);
		assertEquals(4, requestCode);
		assertNotNull(locations);
		assertEquals(3, locations.length);
		assertEquals(2, locations[1]);
	}

	@Test
	public void onInvalidDataReceived() {
		final MutableData data = new MutableData(new byte[] { 0x01, 0x01, 0x00, 0x00, 0x00});
		response.onDataReceived(null, data);
		assertFalse(success);
		assertEquals(0, errorCode);
		assertFalse(response.isValid());
		assertEquals(0, requestCode);
		assertNull(locations);
	}
}