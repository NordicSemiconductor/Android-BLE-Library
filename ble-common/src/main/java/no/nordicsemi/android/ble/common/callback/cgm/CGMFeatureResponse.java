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

package no.nordicsemi.android.ble.common.callback.cgm;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * Response class that could be used as a result of a synchronous request.
 * The data received are available through getters, instead of a callback.
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     CGMFeatureResponse response = readCharacteristic(characteristic)
 *           .awaitValid(CGMFeatureResponse.class);
 *    if (response.getFeatures().calibrationSupported) {
 *        ...
 *    }
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
public final class CGMFeatureResponse extends CGMFeatureDataCallback implements CRCSecuredResponse, Parcelable {
	private CGMFeatures features;
	private int type;
	private int sampleLocation;
	private boolean secured;
	private boolean crcValid;

	public CGMFeatureResponse() {
		// empty
	}

	@Override
	public void onContinuousGlucoseMonitorFeaturesReceived(@NonNull final BluetoothDevice device,
														   @NonNull final CGMFeatures features,
														   final int type, final int sampleLocation,
														   final boolean secured) {
		this.features = features;
		this.type = type;
		this.sampleLocation = sampleLocation;
		this.secured = secured;
		this.crcValid = secured;
	}

	@Override
	public void onContinuousGlucoseMonitorFeaturesReceivedWithCrcError(@NonNull final BluetoothDevice device,
																	   @NonNull final Data data) {
		onInvalidDataReceived(device, data);
		this.secured = true;
		this.crcValid = false;
	}

	public CGMFeatures getFeatures() {
		return features;
	}

	public int getType() {
		return type;
	}

	public int getSampleLocation() {
		return sampleLocation;
	}

	@Override
	public boolean isSecured() {
		return secured;
	}

	@Override
	public boolean isCrcValid() {
		return crcValid;
	}

	// Parcelable
	private CGMFeatureResponse(final Parcel in) {
		super(in);
		if (in.readByte() == 0) {
			features = null;
		} else {
			features = new CGMFeatures(in.readInt());
		}
		type = in.readInt();
		sampleLocation = in.readInt();
		secured = in.readByte() != 0;
		crcValid = in.readByte() != 0;
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
		dest.writeInt(type);
		dest.writeInt(sampleLocation);
		dest.writeByte((byte) (secured ? 1 : 0));
		dest.writeByte((byte) (crcValid ? 1 : 0));
	}

	public static final Creator<CGMFeatureResponse> CREATOR = new Creator<CGMFeatureResponse>() {
		@Override
		public CGMFeatureResponse createFromParcel(final Parcel in) {
			return new CGMFeatureResponse(in);
		}

		@Override
		public CGMFeatureResponse[] newArray(final int size) {
			return new CGMFeatureResponse[size];
		}
	};
}
