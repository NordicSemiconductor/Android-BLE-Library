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

package no.nordicsemi.android.ble.response;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.MtuCallback;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MtuResult implements MtuCallback, Parcelable {
	private BluetoothDevice device;

	@IntRange(from = 23, to = 517)
	private int mtu;

	@Override
	public void onMtuChanged(@NonNull final BluetoothDevice device,
							 @IntRange(from = 23, to = 517) final int mtu) {
		this.device = device;
		this.mtu = mtu;
	}

	@Nullable
	public BluetoothDevice getBluetoothDevice() {
		return device;
	}

	/**
	 * Returns the agreed MTU. The maximum packet size is 3 bytes less then MTU.
	 *
	 * @return The MTU.
	 */
	@IntRange(from = 23, to = 517)
	public int getMtu() {
		return mtu;
	}

	// Parcelable
	protected MtuResult(final Parcel in) {
		device = in.readParcelable(BluetoothDevice.class.getClassLoader());
		mtu = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeParcelable(device, flags);
		dest.writeInt(mtu);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<MtuResult> CREATOR = new Creator<MtuResult>() {
		@Override
		public MtuResult createFromParcel(final Parcel in) {
			return new MtuResult(in);
		}

		@Override
		public MtuResult[] newArray(final int size) {
			return new MtuResult[size];
		}
	};
}
