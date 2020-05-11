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

package no.nordicsemi.android.ble.common.callback.glucose;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.common.profile.glucose.GlucoseMeasurementCallback;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class GlucoseMeasurementDataCallbackTest {
	private boolean success;
	private boolean invalidData;

	private final DataReceivedCallback callback = new GlucoseMeasurementDataCallback() {
		@Override
		public void onGlucoseMeasurementReceived(@NonNull final BluetoothDevice device, final int sequenceNumber,
												 @NonNull final Calendar time, @Nullable final Float glucoseConcentration,
												 @Nullable final Integer unit, @Nullable final Integer type,
												 @Nullable final Integer sampleLocation, @Nullable final GlucoseStatus status,
												 final boolean contextInformationFollows) {
			success = true;
			assertEquals(1, sequenceNumber);
			assertNotNull(time);
			assertEquals(5, time.get(Calendar.MINUTE));
			assertNotNull(unit);
			assertEquals(GlucoseMeasurementCallback.UNIT_kg_L, unit.intValue());
			assertNotNull(status);
			assertEquals(261, status.value);
			assertTrue(status.deviceBatteryLow);
			assertFalse(status.generalDeviceFault);
			assertTrue(contextInformationFollows);
		}

		@Override
		public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
			invalidData = true;
		}
	};

	@Test
	public void onGlucoseMeasurementReceived() {
		final Data data = new Data(new byte[] {
				(byte) 0b011011,           // Time Offset, Type and Location Present, unit: kg/L, Status Annunciation Present, Context follows
				1, 0,                      // Seq = 1
				(byte) 0xE3, 0x07,         // 2019
				2,                         // February
				27,                        // 27th
				11, 10, 30,                // at 11:10:30
				(byte) 0xFB, (byte) 0xFF,  // Time offset = -5 minutes
				30, 0,                     // Glucose concentration = 30.0 kg/L
				0x12,                      // Type = 2 (TYPE_CAPILLARY_PLASMA), Location = 1 (SAMPLE_LOCATION_FINGER)
				0b101, 0b1                 // Status: Low battery, Device Fault, Sensor Temp too low
		});
		callback.onDataReceived(null, data);
		assertTrue(success);
	}

	@Test
	public void onInvalidDataReceived() {
		final Data data = new Data(new byte[] { (byte) 0b011011, 0, 0, (byte) 0xE2, 0x07, 4, 26, 11, 9, 30, 30, 0, 0x12, 0b101, 0b1 }); // time offset missing
		callback.onDataReceived(null, data);
		assertTrue(invalidData);
	}

}