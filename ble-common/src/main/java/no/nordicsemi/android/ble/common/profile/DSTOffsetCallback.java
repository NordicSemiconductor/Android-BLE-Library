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

package no.nordicsemi.android.ble.common.profile;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DSTOffsetCallback {
	enum DSTOffset {
		STANDARD_TIME(0),
		HALF_AN_HOUR_DAYLIGHT_TIME(2),
		DAYLIGHT_TIME(4),
		DOUBLE_DAYLIGHT_TIME(8),
		UNKNOWN(255);

		/**
		 * Offset of the Daylight Saving Time in minutes.
		 */
		public final int offset;

		DSTOffset(final int code) {
			if (code != 255)
				this.offset = code * 15; // convert to minutes
			else
				this.offset = 0;
		}

		@Nullable
		public static DSTOffset from(final int value) {
			switch (value) {
				case 0: return STANDARD_TIME;
				case 2: return HALF_AN_HOUR_DAYLIGHT_TIME;
				case 4: return DAYLIGHT_TIME;
				case 8: return DOUBLE_DAYLIGHT_TIME;
				case 255: return UNKNOWN;
				default: return null;
			}
		}
	}

	/**
	 * Callback called when DST Offset packet has been received.
	 *
	 * @param device the target device.
	 * @param offset the Daylight Saving Time offset.
	 */
	void onDSTOffsetReceived(@NonNull final BluetoothDevice device, @NonNull final DSTOffset offset);
}
