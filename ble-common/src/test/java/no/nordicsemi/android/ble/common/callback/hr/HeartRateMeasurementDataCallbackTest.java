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

package no.nordicsemi.android.ble.common.callback.hr;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.List;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;

import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class HeartRateMeasurementDataCallbackTest {
	private boolean success;
	private int heartRate;
	private Boolean contactDetected;
	private Integer energyExpanded;
	private List<Integer> rrIntervals;

	private final ProfileReadResponse response = new HeartRateMeasurementDataCallback() {

		@Override
		public void onHeartRateMeasurementReceived(@NonNull final BluetoothDevice device,
												   final int heartRate,
												   @Nullable final Boolean contactDetected,
												   @Nullable final Integer energyExpanded,
												   @Nullable final List<Integer> rrIntervals) {
			HeartRateMeasurementDataCallbackTest.this.success = true;
			HeartRateMeasurementDataCallbackTest.this.heartRate = heartRate;
			HeartRateMeasurementDataCallbackTest.this.contactDetected = contactDetected;
			HeartRateMeasurementDataCallbackTest.this.energyExpanded = energyExpanded;
			HeartRateMeasurementDataCallbackTest.this.rrIntervals = rrIntervals;
		}
	};

	@Test
	public void onHeartRateMeasurementReceived_simple() {
		success = false;
		final Data data = new Data(new byte[] { 0, 85 });
		response.onDataReceived(null, data);
		assertTrue(response.isValid());
		assertTrue(success);
		assertEquals(85, heartRate);
		assertNull(contactDetected);
		assertNull(energyExpanded);
		assertNull(rrIntervals);
	}

	@Test
	public void onHeartRateMeasurementReceived_noContact() {
		success = false;
		final Data data = new Data(new byte[] {0x4, (byte) 0xFF});
		response.onDataReceived(null, data);
		assertTrue(response.isValid());
		assertTrue(success);
		assertEquals(255, heartRate);
		assertNotNull(contactDetected);
		assertFalse(contactDetected);
		assertNull(energyExpanded);
		assertNull(rrIntervals);
	}

	@Test
	public void onHeartRateMeasurementReceived_uint8() {
		success = false;
		final Data data = new Data(new byte[] { 0b11110, 85, 50, 0, 33, 0 });
		response.onDataReceived(null, data);
		assertTrue(response.isValid());
		assertTrue(success);
		assertEquals(85, heartRate);
		assertNotNull(contactDetected);
		assertTrue(contactDetected);
		assertNotNull(energyExpanded);
		assertEquals(50, energyExpanded.intValue());
		assertNotNull(rrIntervals);
		assertEquals(1, rrIntervals.size());
		assertEquals(33, rrIntervals.get(0).intValue());
	}

	@Test
	public void onHeartRateMeasurementReceived_uint16() {
		success = false;
		final Data data = new Data(new byte[] { 0b11111, 1, 1, 50, 1, 1, 0, 2, 1, (byte) 0xFF, (byte) 0xFF});
		response.onDataReceived(null, data);
		assertTrue(response.isValid());
		assertTrue(success);
		assertEquals(257, heartRate);
		assertNotNull(contactDetected);
		assertTrue(contactDetected);
		assertNotNull(energyExpanded);
		assertEquals(306, energyExpanded.intValue());
		assertNotNull(rrIntervals);
		assertEquals(3, rrIntervals.size());
		assertEquals(1, rrIntervals.get(0).intValue());
		assertEquals(258, rrIntervals.get(1).intValue());
		assertEquals(65535, rrIntervals.get(2).intValue());
	}

	@Test
	public void onInvalidDataReceived() {
		success = false;
		final Data data = new Data(new byte[] { 0b10111, 1, 1 });
		response.onDataReceived(null, data);
		assertFalse(response.isValid());
		assertFalse(success);
	}
}