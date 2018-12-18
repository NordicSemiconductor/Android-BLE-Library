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

package no.nordicsemi.android.ble.callback.profile;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.response.ReadResponse;

/**
 * A response type for read requests with basic validation check.
 * When read was requested as a synchronous call the {@link #isValid()} can be used to
 * check if data were parsed successfully. Parsing method must call super methods on
 * both {@link #onDataReceived(BluetoothDevice, Data)} and
 * {@link #onInvalidDataReceived(BluetoothDevice, Data)} in order to make getters working properly.
 * <p>
 * Check out profile data callbacks in the Android BLE Common Library for example of usage.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ProfileReadResponse extends ReadResponse implements ProfileDataCallback, Parcelable {
	private boolean valid = true;

	public ProfileReadResponse() {
		// empty
	}

	@Override
	public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
									  @NonNull final Data data) {
		this.valid = false;
	}

	/**
	 * Returns true if {@link #onInvalidDataReceived(BluetoothDevice, Data)} wasn't called.
	 *
	 * @return True, if profile data were valid, false if parsing error occurred.
	 */
	public boolean isValid() {
		return valid;
	}

	// Parcelable
	protected ProfileReadResponse(final Parcel in) {
		super(in);
		valid = in.readByte() != 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		dest.writeByte((byte) (valid ? 1 : 0));
	}

	public static final Creator<ProfileReadResponse> CREATOR = new Creator<ProfileReadResponse>() {
		@Override
		public ProfileReadResponse createFromParcel(final Parcel in) {
			return new ProfileReadResponse(in);
		}

		@Override
		public ProfileReadResponse[] newArray(final int size) {
			return new ProfileReadResponse[size];
		}
	};
}
