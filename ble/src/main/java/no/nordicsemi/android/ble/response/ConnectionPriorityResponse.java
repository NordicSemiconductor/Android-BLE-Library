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
import no.nordicsemi.android.ble.callback.ConnectionPriorityCallback;

/**
 * The synchronous response type for connection priority requests.
 *
 * @see ConnectionPriorityCallback
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConnectionPriorityResponse implements ConnectionPriorityCallback, Parcelable {
	private BluetoothDevice device;

	@IntRange(from = 6, to = 3200)
	private int interval;

	@IntRange(from = 0, to = 499)
	private int latency;

	@IntRange(from = 10, to = 3200)
	private int supervisionTimeout;

	@Override
	public void onConnectionUpdated(@NonNull final BluetoothDevice device,
									@IntRange(from = 6, to = 3200) final int interval,
									@IntRange(from = 0, to = 499) final int latency,
									@IntRange(from = 10, to = 3200) final int timeout) {
		this.device = device;
		this.interval = interval;
		this.latency = latency;
		this.supervisionTimeout = timeout;
	}

	@Nullable
	public BluetoothDevice getBluetoothDevice() {
		return device;
	}

	/**
	 * The connection interval determines how often the Central will ask for data from the Peripheral.
	 * When the Peripheral requests an update, it supplies a maximum and a minimum wanted interval.
	 * The connection interval must be between 7.5 ms and 4 s.
	 *
	 * @return Connection interval used on this connection, 1.25ms unit.
	 * Valid range is from 6 (7.5ms) to 3200 (4000ms).
	 */
	@IntRange(from = 6, to = 3200)
	public int getConnectionInterval() {
		return interval;
	}

	/**
	 * By setting a non-zero slave latency, the Peripheral can choose to not answer when
	 * the Central asks for data up to the slave latency number of times.
	 * However, if the Peripheral has data to send, it can choose to send data at any time.
	 * This enables a peripheral to stay sleeping for a longer time, if it doesn't have data to send,
	 * but still send data fast if needed. The text book example of such device is for example
	 * keyboard and mice, which want to be sleeping for as long as possible when there is
	 * no data to send, but still have low latency (and for the mouse: low connection interval)
	 * when needed.
	 *
	 * @return Slave latency for the connection in number of connection events.
	 * Valid range is from 0 to 499.
	 */
	@IntRange(from = 0, to = 499)
	public int getSlaveLatency() {
		return latency;
	}

	/**
	 * This timeout determines the timeout from the last data exchange till a link is considered lost.
	 * A Central will not start trying to reconnect before the timeout has passed,
	 * so if you have a device which goes in and out of range often, and you need to notice when
	 * that happens, it might make sense to have a short timeout.
	 *
	 * @return Supervision timeout for this connection, in 10ms unit.
	 * Valid range is from 10 (100 ms = 0.1s) to 3200 (32s).
	 */
	@IntRange(from = 10, to = 3200)
	public int getSupervisionTimeout() {
		return supervisionTimeout;
	}

	// Parcelable
	protected ConnectionPriorityResponse(final Parcel in) {
		device = in.readParcelable(BluetoothDevice.class.getClassLoader());
		interval = in.readInt();
		latency = in.readInt();
		supervisionTimeout = in.readInt();
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeParcelable(device, flags);
		dest.writeInt(interval);
		dest.writeInt(latency);
		dest.writeInt(supervisionTimeout);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<ConnectionPriorityResponse> CREATOR = new Creator<ConnectionPriorityResponse>() {
		@Override
		public ConnectionPriorityResponse createFromParcel(final Parcel in) {
			return new ConnectionPriorityResponse(in);
		}

		@Override
		public ConnectionPriorityResponse[] newArray(final int size) {
			return new ConnectionPriorityResponse[size];
		}
	};
}
