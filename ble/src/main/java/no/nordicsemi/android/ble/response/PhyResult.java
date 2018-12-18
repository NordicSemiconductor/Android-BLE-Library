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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.annotation.PhyValue;
import no.nordicsemi.android.ble.callback.PhyCallback;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PhyResult implements PhyCallback, Parcelable {
	private BluetoothDevice device;

	@PhyValue
	private int txPhy;

	@PhyValue
	private int rxPhy;

	@Override
	public void onPhyChanged(@NonNull final BluetoothDevice device,
							 @PhyValue final int txPhy, @PhyValue final int rxPhy) {
		this.device = device;
		this.txPhy = txPhy;
		this.rxPhy = rxPhy;
	}

	@Nullable
	public BluetoothDevice getBluetoothDevice() {
		return device;
	}

	@PhyValue
	public int getTxPhy() {
		return txPhy;
	}

	@PhyValue
	public int getRxPhy() {
		return rxPhy;
	}

	// Parcelable
	protected PhyResult(final Parcel in) {
		device = in.readParcelable(BluetoothDevice.class.getClassLoader());
		txPhy = in.readInt();
		rxPhy = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeParcelable(device, flags);
		dest.writeInt(txPhy);
		dest.writeInt(rxPhy);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<PhyResult> CREATOR = new Creator<PhyResult>() {
		@Override
		public PhyResult createFromParcel(final Parcel in) {
			return new PhyResult(in);
		}

		@Override
		public PhyResult[] newArray(final int size) {
			return new PhyResult[size];
		}
	};
}
