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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.ClosedCallback;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.ReadProgressCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataFilter;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataStream;
import no.nordicsemi.android.ble.data.PacketFilter;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ValueChangedCallback {
	private static final String TAG = ValueChangedCallback.class.getSimpleName();

	private ClosedCallback closedCallback;
	private ReadProgressCallback progressCallback;
	private DataReceivedCallback valueCallback;
	private DataMerger dataMerger;
	private DataStream buffer;
	private DataFilter filter;
	private PacketFilter packetFilter;
	private CallbackHandler handler;
	private int count = 0;

	ValueChangedCallback(final CallbackHandler handler) {
		this.handler = handler;
	}

	@NonNull
	public ValueChangedCallback setHandler(@Nullable final Handler handler) {
		this.handler = new CallbackHandler() {
			@Override
			public void post(@NonNull final Runnable r) {
				if (handler != null)
					handler.post(r);
				else
					r.run();
			}

			@Override
			public void postDelayed(@NonNull final Runnable r, final long delayMillis) {
				// not used
			}

			@Override
			public void removeCallbacks(@NonNull final Runnable r) {
				// not used
			}
		};
		return this;
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
	 * <p>
	 * This filter filters each received packet before they are given to the data merger.
	 * To filter the complete packet after merging, use {@link #filterPacket(PacketFilter)} instead.
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
	 * Sets a packet filter which allows to ignore the complete packet.
	 * <p>
	 * This filter differs from the {@link #filter(DataFilter)}, as it checks the complete
	 * packet, after it has been merged. If there is not merger set, this does the same as
	 * the data filter.
	 *
	 * @param filter the packet filter.
	 * @since 2.4.0
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback filterPacket(@NonNull final PacketFilter filter) {
		this.packetFilter = filter;
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

	/**
	 * Sets a callback that will be executed when the device services were invalidated (i.e. on
	 * disconnection) or the callback has been unregistered and it can release resources.
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	public ValueChangedCallback then(@NonNull final ClosedCallback callback) {
		this.closedCallback = callback;
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

		if (dataMerger == null && (packetFilter == null || packetFilter.filter(value))) {
			final Data data = new Data(value);
			handler.post(() -> {
				try {
					valueCallback.onDataReceived(device, data);
				} catch (final Throwable t) {
					Log.e(TAG, "Exception in Value callback", t);
				}
			});
		} else {
			handler.post(() -> {
				if (progressCallback != null) {
					try {
						progressCallback.onPacketReceived(device, value, count);
					} catch (final Throwable t) {
						Log.e(TAG, "Exception in Progress callback", t);
					}
				}
			});
			if (buffer == null)
				buffer = new DataStream();
			if (dataMerger.merge(buffer, value, count++)) {
				final byte[] merged = buffer.toByteArray();
				if (packetFilter == null || packetFilter.filter(merged)) {
					final Data data = new Data(merged);
					handler.post(() -> {
						try {
							valueCallback.onDataReceived(device, data);
						} catch (final Throwable t) {
							Log.e(TAG, "Exception in Value callback", t);
						}
					});
				}
				buffer = null;
				count = 0;
			} // else
			// wait for more packets to be merged
		}
	}

	void notifyClosed() {
		if (closedCallback != null) {
			try {
				closedCallback.onClosed();
			} catch (final Throwable t) {
				Log.e(TAG, "Exception in Closed callback", t);
			}
		}
		free();
	}

	private void free() {
		closedCallback = null;
		valueCallback = null;
		dataMerger = null;
		progressCallback = null;
		filter = null;
		packetFilter = null;
		buffer = null;
		count = 0;
	}
}
