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

package no.nordicsemi.android.ble.common.callback.rsc;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.MutableData;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class RunningSpeedAndCadenceMeasurementDataCallbackTest {
	private boolean called;
	private boolean invalidData;

	@Test
	public void onRSCMeasurementReceived() {
		final ProfileReadResponse response = new RunningSpeedAndCadenceMeasurementDataCallback() {
			@Override
			public void onRSCMeasurementReceived(@NonNull final BluetoothDevice device, final boolean running,
												 final float instantaneousSpeed, final int instantaneousCadence,
												 @Nullable final Integer strideLength,
												 @Nullable final Long totalDistance) {
				called = true;
				assertTrue("Running", running);
				assertEquals("Speed", 3, instantaneousSpeed, 0f);
				assertEquals("cadence", 18, instantaneousCadence);
				assertNotNull("Stride Length present", strideLength);
				assertEquals("Stride Length", 86, strideLength.intValue());
				assertNotNull("Total Distance present", totalDistance);
				assertEquals("Total Distance", 0xF0000001L, totalDistance.longValue());
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				super.onInvalidDataReceived(device, data);
				invalidData = true;
			}
		};

		final MutableData data = new MutableData(new byte[10]);
		data.setByte(0x7, 0);
		data.setValue(3 * 256, Data.FORMAT_UINT16, 1);
		data.setValue(18, Data.FORMAT_UINT8, 3);
		data.setValue(86, Data.FORMAT_UINT16, 4);
		data.setValue(0xF0000001L, Data.FORMAT_UINT32, 6);
		called = false;
		response.onDataReceived(null, data);
		assertTrue(response.isValid());
		assertTrue(called);
		assertFalse(invalidData);
	}

	@Test
	public void onInvalidDataReceived() {
		final ProfileReadResponse response = new RunningSpeedAndCadenceMeasurementDataCallback() {
			@Override
			public void onRSCMeasurementReceived(@NonNull final BluetoothDevice device, final boolean running,
												 final float instantaneousSpeed, final int instantaneousCadence,
												 @Nullable final Integer strideLength,
												 @Nullable final Long totalDistance) {
				called = true;
			}

			@Override
			public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
				super.onInvalidDataReceived(device, data);
				invalidData = true;
			}
		};

		final MutableData data = new MutableData(new byte[9]); // too short
		data.setByte(0x7, 0);
		data.setValue(3 * 256, Data.FORMAT_UINT16, 1);
		data.setValue(18, Data.FORMAT_UINT8, 3);
		data.setValue(86, Data.FORMAT_UINT16, 4);
		data.setValue(0, Data.FORMAT_UINT24, 6);
		called = false;
		invalidData = false;
		response.onDataReceived(null, data);
		assertFalse(response.isValid());
		assertFalse(called);
		assertTrue(invalidData);
	}
}