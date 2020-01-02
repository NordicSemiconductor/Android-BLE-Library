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

package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.ReadProgressCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataFilter;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataStream;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ValueChangedCallback {
	private ReadProgressCallback progressCallback;
	private DataReceivedCallback valueCallback;
	private DataMerger dataMerger;
	private DataStream buffer;
	private DataFilter filter;
	private final Handler handler;
	private int count = 0;

	ValueChangedCallback(final Handler handler) {
		this.handler = handler;
	}

	/**
	 * Sets the asynchronous data callback that will be called whenever a notification or
	 * an indication is received on given characteristic.
	 *
	 * @param callback the data callback.
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback with(@NonNull final DataReceivedCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	/**
	 * Sets a filter which allows to skip some incoming data.
	 *
	 * @param filter the data filter.
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback filter(@NonNull final DataFilter filter) {
		this.filter = filter;
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback merge(@NonNull final DataMerger merger) {
		this.dataMerger = merger;
		this.progressCallback = null;
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback merge(@NonNull final DataMerger merger,
									  @NonNull final ReadProgressCallback callback) {
		this.dataMerger = merger;
		this.progressCallback = callback;
		return this;
	}

	ValueChangedCallback free() {
		valueCallback = null;
		dataMerger = null;
		progressCallback = null;
		buffer = null;
		return this;
	}

	boolean matches(final byte[] packet) {
		return filter == null || filter.filter(packet);
	}

	void notifyValueChanged(@NonNull final BluetoothDevice device, @Nullable final byte[] value) {
		// Keep a reference to the value callback, as it may change during execution
		final DataReceivedCallback valueCallback = this.valueCallback;

		// With no value callback there is no need for any merging
		if (valueCallback == null) {
			return;
		}

		if (dataMerger == null) {
			final Data data = new Data(value);
			handler.post(() -> valueCallback.onDataReceived(device, data));
		} else {
			handler.post(() -> {
				if (progressCallback != null)
					progressCallback.onPacketReceived(device, value, count);
			});
			if (buffer == null)
				buffer = new DataStream();
			if (dataMerger.merge(buffer, value, count++)) {
				final Data data = buffer.toData();
				handler.post(() -> valueCallback.onDataReceived(device, data));
				buffer = null;
				count = 0;
			} // else
			// wait for more packets to be merged
		}
	}
}
