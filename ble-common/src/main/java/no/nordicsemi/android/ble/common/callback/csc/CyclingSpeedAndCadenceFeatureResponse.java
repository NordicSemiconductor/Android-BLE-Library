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

package no.nordicsemi.android.ble.common.callback.csc;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     CyclingSpeedAndCadenceFeatureResponse response = readCharacteristic(characteristic)
 *           .awaitValid(CyclingSpeedAndCadenceFeatureResponse.class);
 *     CSCFeatures features = response.getFeatures();
 *     ...
 * } catch ({@link RequestFailedException} e) {
 *     Log.w(TAG, "Request failed with status " + e.getStatus(), e);
 * } catch ({@link InvalidDataException} e) {
 *     Log.w(TAG, "Invalid data received: " + e.getResponse().getRawData());
 * }
 * </pre>
 * </p>
 */
@SuppressWarnings("unused")
public final class CyclingSpeedAndCadenceFeatureResponse extends CyclingSpeedAndCadenceFeatureDataCallback implements Parcelable {
	@Nullable
	private CSCFeatures features;

	public CyclingSpeedAndCadenceFeatureResponse() {
		// empty
	}

	@Override
	public void onCyclingSpeedAndCadenceFeaturesReceived(@NonNull final BluetoothDevice device,
														 @NonNull final CSCFeatures features) {
		this.features = features;
	}

	@Nullable
	public CSCFeatures getFeatures() {
		return features;
	}

	// Parcelable
	private CyclingSpeedAndCadenceFeatureResponse(final Parcel in) {
		super(in);
		if (in.readByte() == 0) {
			features = null;
		} else {
			features = new CSCFeatures(in.readInt());
		}
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		super.writeToParcel(dest, flags);
		if (features == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeInt(features.value);
		}
	}

	public static final Creator<CyclingSpeedAndCadenceFeatureResponse> CREATOR = new Creator<CyclingSpeedAndCadenceFeatureResponse>() {
		@Override
		public CyclingSpeedAndCadenceFeatureResponse createFromParcel(final Parcel in) {
			return new CyclingSpeedAndCadenceFeatureResponse(in);
		}

		@Override
		public CyclingSpeedAndCadenceFeatureResponse[] newArray(final int size) {
			return new CyclingSpeedAndCadenceFeatureResponse[size];
		}
	};
}
