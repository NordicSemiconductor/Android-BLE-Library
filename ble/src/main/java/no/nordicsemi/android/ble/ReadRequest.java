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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.ReadProgressCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataFilter;
import no.nordicsemi.android.ble.data.DataMerger;
import no.nordicsemi.android.ble.data.DataStream;
import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidDataException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class ReadRequest extends SimpleValueRequest<DataReceivedCallback> implements Operation  {
	private ReadProgressCallback progressCallback;
	private DataMerger dataMerger;
	private DataStream buffer;
	private DataFilter filter;
	private int count = 0;

	ReadRequest(@NonNull final Type type) {
		super(type);
	}

	ReadRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ReadRequest(@NonNull final Type type, @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	@NonNull
	@Override
	ReadRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public ReadRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	@Override
	@NonNull
	public ReadRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public ReadRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public ReadRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public ReadRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	@Override
	@NonNull
	public ReadRequest with(@NonNull final DataReceivedCallback callback) {
		super.with(callback);
		return this;
	}

	/**
	 * Sets a filter which allows to skip some incoming data.
	 *
	 * @param filter the data filter.
	 * @return The request.
	 */
	@NonNull
	public ReadRequest filter(@NonNull final DataFilter filter) {
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
	public ReadRequest merge(@NonNull final DataMerger merger) {
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
	public ReadRequest merge(@NonNull final DataMerger merger,
							 @NonNull final ReadProgressCallback callback) {
		this.dataMerger = merger;
		this.progressCallback = callback;
		return this;
	}

	/**
	 * Same as {@link #await(Class)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is not valid
	 * ({@link ProfileReadResponse#isValid()} returns false), this method will
	 * throw an exception.
	 *
	 * @param responseClass the response class. This class will be instantiate, therefore it
	 *                      has to have a default constructor.
	 * @return The object with the response.
	 * @throws RequestFailedException      thrown when the BLE request finished with status other
     *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException       thrown when you try to call this method from the main
     *                                     (UI) thread.
	 * @throws IllegalArgumentException    thrown when the response class could not be instantiated.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     *                                     {@link ProfileReadResponse#onDataReceived(BluetoothDevice, Data)}
     *                                     failed to parse the data correctly and called
     *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}).
	 * @throws InvalidRequestException     thrown when the request was called before the device was
	 *                                     connected at least once (unknown device).
	 */
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final Class<E> responseClass)
			throws RequestFailedException, InvalidDataException, DeviceDisconnectedException,
			BluetoothDisabledException, InvalidRequestException {
		final E response = await(responseClass);
		if (!response.isValid()) {
			throw new InvalidDataException(response);
		}
		return response;
	}

	/**
	 * Same as {@link #await(Object)}, but if the response class extends
	 * {@link ProfileReadResponse} and the received response is not valid
	 * ({@link ProfileReadResponse#isValid()} returns false), this method will
	 * throw an exception.
	 *
	 * @param response the response object.
	 * @return The object with the response.
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     *                                     than {@link BluetoothGatt#GATT_SUCCESS}.
     * @throws IllegalStateException       thrown when you try to call this method from the main
     *                                     (UI) thread.
	 * @throws DeviceDisconnectedException thrown when the device disconnected before the request
	 *                                     was completed.
	 * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidDataException        thrown when the received data were not valid (that is when
     *                                     {@link ProfileReadResponse#onDataReceived(BluetoothDevice, Data)}
     *                                     failed to parse the data correctly and called
     *                                     {@link ProfileReadResponse#onInvalidDataReceived(BluetoothDevice, Data)}).
	 * @throws InvalidRequestException     thrown when the request was called before the device was
	 *                                     connected at least once (unknown device).
	 */
	@NonNull
	public <E extends ProfileReadResponse> E awaitValid(@NonNull final E response)
			throws RequestFailedException, InvalidDataException, DeviceDisconnectedException,
			BluetoothDisabledException, InvalidRequestException {
		await(response);
		if (!response.isValid()) {
			throw new InvalidDataException(response);
		}
		return response;
	}

	boolean matches(final byte[] packet) {
		return filter == null || filter.filter(packet);
	}

	void notifyValueChanged(@NonNull final BluetoothDevice device, @Nullable final byte[] value) {
		// Keep a reference to the value callback, as it may change during execution
		final DataReceivedCallback valueCallback = this.valueCallback;

		// With no value callback there is no need for any merging
		if (valueCallback == null)
			return;

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

	boolean hasMore() {
		return count > 0;
	}
}
