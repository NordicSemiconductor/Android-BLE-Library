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
import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.common.profile.DSTOffsetCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * Data callback that parses 1-byte value into a {@link DSTOffset}.
 * If the value received is shorter than 1 byte the
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} callback will be called.
 * See: https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.dst_offset.xml
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public abstract class DSTOffsetDataCallback extends ProfileReadResponse implements DSTOffsetCallback {

	public DSTOffsetDataCallback() {
		// empty
	}

	protected DSTOffsetDataCallback(final Parcel in) {
		super(in);
	}

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		super.onDataReceived(device, data);

		final DSTOffset offset = readDSTOffset(data, 0);
		if (offset == null) {
			onInvalidDataReceived(device, data);
			return;
		}
		onDSTOffsetReceived(device, offset);
	}

	/**
	 * Returns the Daylight Saving Time Offset, or null if offset is out of data range.
	 *
	 * @param data input data.
	 * @param offset offset to read DST Offset from.
	 * @return DSTOffset field or null.
	 */
	@Nullable
	public static DSTOffset readDSTOffset(@NonNull final Data data, final int offset) {
		if (data.size() < offset + 1)
			return null;

		final int o = data.getIntValue(Data.FORMAT_UINT8, offset);
		return DSTOffset.from(o);
	}
}
