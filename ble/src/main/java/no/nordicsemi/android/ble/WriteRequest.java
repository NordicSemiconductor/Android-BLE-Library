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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;

import java.util.Arrays;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.annotation.WriteType;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.WriteProgressCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataSplitter;
import no.nordicsemi.android.ble.data.DefaultMtuSplitter;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class WriteRequest extends SimpleValueRequest<DataSentCallback> implements Operation {
	private final static DataSplitter MTU_SPLITTER = new DefaultMtuSplitter();

	private WriteProgressCallback progressCallback;
	private DataSplitter dataSplitter;
	private final byte[] data;
	private final int writeType;
	private byte[] currentChunk;
	private byte[] nextChunk;
	private int count = 0;
	private boolean complete = false;

	WriteRequest(@NonNull final Type type) {
		this(type, null);
	}

	WriteRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
		// not used:
		this.data = null;
		this.writeType = 0;
		// getData(int) isn't called on enabling and disabling notifications/indications.
		this.complete = true;
	}

	WriteRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic,
				 @Nullable final byte[] data,
				 @IntRange(from = 0) final int offset, @IntRange(from = 0) final int length,
				 @WriteType final int writeType) {
		super(type, characteristic);
		this.data = Bytes.copy(data, offset, length);
		this.writeType = writeType;
	}

	WriteRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic,
				 @Nullable final byte[] data,
				 @IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		super(type, characteristic);
		this.data = Bytes.copy(data, offset, length);
		this.writeType = 0;
	}

	WriteRequest(@NonNull final Type type, @Nullable final BluetoothGattDescriptor descriptor,
				 @Nullable final byte[] data,
				 @IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		super(type, descriptor);
		this.data = Bytes.copy(data, offset, length);
		this.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
	}

	@NonNull
	@Override
	WriteRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public WriteRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	@Override
	@NonNull
	public WriteRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public WriteRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public WriteRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public WriteRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	@Override
	@NonNull
	public WriteRequest with(@NonNull final DataSentCallback callback) {
		super.with(callback);
		return this;
	}

	/**
	 * Adds a splitter that will be used to cut given data into multiple packets.
	 * The splitter may modify each packet if necessary, i.e. add a flag indicating first packet,
	 * continuation or the last packet.
	 *
	 * @param splitter an implementation of a splitter.
	 * @return The request.
	 * @see #split()
	 */
	@NonNull
	public WriteRequest split(@NonNull final DataSplitter splitter) {
		this.dataSplitter = splitter;
		this.progressCallback = null;
		return this;
	}

	/**
	 * Adds a splitter that will be used to cut given data into multiple packets.
	 * The splitter may modify each packet if necessary, i.e. add a flag indicating first packet,
	 * continuation or the last packet.
	 *
	 * @param splitter an implementation of a splitter.
	 * @param callback the progress callback that will be notified each time a packet was sent.
	 * @return The request.
	 * @see #split()
	 */
	@NonNull
	public WriteRequest split(@NonNull final DataSplitter splitter,
							  @NonNull final WriteProgressCallback callback) {
		this.dataSplitter = splitter;
		this.progressCallback = callback;
		return this;
	}

	/**
	 * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
	 * bytes long packets.
	 *
	 * @return The request.
	 */
	@NonNull
	public WriteRequest split() {
		this.dataSplitter = MTU_SPLITTER;
		this.progressCallback = null;
		return this;
	}

	/**
	 * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
	 * bytes long packets.
	 *
	 * @param callback the progress callback that will be notified each time a packet was sent.
	 * @return The request.
	 */
	@NonNull
	public WriteRequest split(@NonNull final WriteProgressCallback callback) {
		this.dataSplitter = MTU_SPLITTER;
		this.progressCallback = callback;
		return this;
	}

	/**
	 * This method makes sure the data sent will be split to at-most MTU-3 bytes long packets.
	 * This is because Long Write does not work with Reliable Write.
	 */
	void forceSplit() {
		if (dataSplitter == null)
			split();
	}

	/**
	 * Returns the next chunk to be sent. If data splitter was not set the date returned may
	 * be longer than MTU. Android will try to send them using Long Write sub-procedure if
	 * write type is {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}. Other write types
	 * will cause the data to be truncated.
	 *
	 * @param mtu the current MTU.
	 * @return The next bytes to be sent.
	 */
	byte[] getData(@IntRange(from = 23, to = 517) final int mtu) {
		if (dataSplitter == null || data == null) {
			complete = true;
			return currentChunk = data;
		}

		// Write Request and Write Command require 3 bytes for handler and op code.
		// Write Signed requires 12 bytes, as the signature is sent.
		final int maxLength = writeType != BluetoothGattCharacteristic.WRITE_TYPE_SIGNED ?
				mtu - 3 : mtu - 12;

		byte[] chunk = nextChunk;
		// Get the first chunk.
		if (chunk == null) {
			chunk = dataSplitter.chunk(data, count, maxLength);
		}
		// If there's something to send, check if there are any more packets to be sent later.
		if (chunk != null) {
			nextChunk = dataSplitter.chunk(data, count + 1, maxLength);
		}
		// If there's no next packet left, we are done.
		if (nextChunk == null) {
			complete = true;
		}
		return currentChunk = chunk;
	}

	/**
	 * Method called when packet has been sent and confirmed (when Write With Response was used),
	 * or added to local outgoing buffer (when Write Without Response was used).
	 *
	 * @param device the target device.
	 * @param data   the data received in the
	 *               {@link android.bluetooth.BluetoothGattCallback#onCharacteristicWrite(BluetoothGatt, BluetoothGattCharacteristic, int)}.
	 * @return True, if the data received are equal to data sent.
	 */
	boolean notifyPacketSent(@NonNull final BluetoothDevice device, @Nullable final byte[] data) {
		handler.post(() -> {
			if (progressCallback != null)
				progressCallback.onPacketSent(device, data, count);
		});
		count++;
		if (complete) {
			handler.post(() -> {
				if (valueCallback != null)
					valueCallback.onDataSent(device, new Data(WriteRequest.this.data));
			});
		}
		return Arrays.equals(data, currentChunk);
	}

	/**
	 * Returns whether there are more bytes to be sent from this Write Request.
	 *
	 * @return True if not all data were sent, false if the request is complete.
	 */
	boolean hasMore() {
		return !complete;
	}

	/**
	 * Returns the write type that should be used to send the data.
	 *
	 * @return The write type.
	 */
	@WriteType
	int getWriteType() {
		return writeType;
	}
}
