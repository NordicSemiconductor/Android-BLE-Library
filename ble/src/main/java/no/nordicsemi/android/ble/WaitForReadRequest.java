package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
public final class WaitForReadRequest extends AwaitingRequest<DataSentCallback> implements Operation {
	private static final DataSplitter MTU_SPLITTER = new DefaultMtuSplitter();

	private WriteProgressCallback progressCallback;
	private DataSplitter dataSplitter;
	private byte[] data;
	private byte[] nextChunk;
	private int count = 0;
	private boolean complete = false;

	WaitForReadRequest(@NonNull final Request.Type type, @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
		// not used:
		this.data = null;
		// data might have been set earlier using SetValueRequest.
		this.complete = true;
	}

	WaitForReadRequest(@NonNull final Request.Type type, @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
		// not used:
		this.data = null;
		// data might have been set earlier using SetValueRequest.
		this.complete = true;
	}

	WaitForReadRequest(@NonNull final Request.Type type, @Nullable final BluetoothGattCharacteristic characteristic,
					   @Nullable final byte[] data,
					   @IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		super(type, characteristic);
		this.data = Bytes.copy(data, offset, length);
	}

	WaitForReadRequest(@NonNull final Request.Type type, @Nullable final BluetoothGattDescriptor descriptor,
					   @Nullable final byte[] data,
					   @IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		super(type, descriptor);
		this.data = Bytes.copy(data, offset, length);
	}

	void setDataIfNull(@Nullable final byte[] data) {
		if (this.data == null)
			this.data = data;
	}

	@NonNull
	@Override
	WaitForReadRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public WaitForReadRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	@Override
	@NonNull
	public WaitForReadRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public WaitForReadRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public WaitForReadRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public WaitForReadRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	@Override
	@NonNull
	public WaitForReadRequest with(@NonNull final DataSentCallback callback) {
		super.with(callback);
		return this;
	}

	@NonNull
	public WaitForReadRequest trigger(@NonNull final Operation trigger) {
		super.trigger(trigger);
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
	public WaitForReadRequest split(@NonNull final DataSplitter splitter) {
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
	public WaitForReadRequest split(@NonNull final DataSplitter splitter,
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
	public WaitForReadRequest split() {
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
	public WaitForReadRequest split(@NonNull final WriteProgressCallback callback) {
		this.dataSplitter = MTU_SPLITTER;
		this.progressCallback = callback;
		return this;
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
			return data;
		}

		// Read [procedure requires 3 bytes for handler and op code.
		final int maxLength = mtu - 3;

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
		return chunk;
	}

	/**
	 * Method called when packet has been read by the remote device.
	 *
	 * @param device the target device.
	 * @param data   the data sent in {@link android.bluetooth.BluetoothGattServer#sendResponse(BluetoothDevice, int, int, int, byte[])}
	 *               from
	 *               {@link android.bluetooth.BluetoothGattServerCallback#onCharacteristicReadRequest(BluetoothDevice, int, int, BluetoothGattCharacteristic)}
	 *               or
	 *               {@link android.bluetooth.BluetoothGattServerCallback#onDescriptorReadRequest(BluetoothDevice, int, int, BluetoothGattDescriptor)}
	 */
	void notifyPacketRead(@NonNull final BluetoothDevice device, @Nullable final byte[] data) {
		handler.post(() -> {
			if (progressCallback != null)
				progressCallback.onPacketSent(device, data, count);
		});
		count++;
	}

	@Override
	void notifySuccess(@NonNull final BluetoothDevice device) {
		handler.post(() -> {
			if (valueCallback != null)
				valueCallback.onDataSent(device, new Data(data));
		});
		super.notifySuccess(device);
	}

	/**
	 * Returns whether there are more bytes to be sent.
	 *
	 * @return True if not all data were sent, false if the request is complete.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	boolean hasMore() {
		return !complete;
	}
}
