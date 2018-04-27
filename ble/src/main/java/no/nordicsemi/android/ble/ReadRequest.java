package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.data.DataMerger;

public final class ReadRequest extends Request {
	private DataReceivedCallback valueCallback;
	private DataMerger dataMerger;
	private ByteArrayOutputStream buffer;
	private int count = 0;

	ReadRequest(final @NonNull Type type) {
		super(type);
	}

	ReadRequest(final @NonNull Type type, final @Nullable BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ReadRequest(final @NonNull Type type, final @Nullable BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	@Override
	@NonNull
	public ReadRequest done(final @NonNull SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public ReadRequest fail(final @NonNull FailCallback callback) {
		this.failCallback = callback;
		return this;
	}

	@NonNull
	public ReadRequest with(final @NonNull DataReceivedCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	/**
	 * Adds a merger that will be used to merge multiple packets into a single Data.
	 * The merger may modify each packet if necessary.
	 *
	 * @return the request
	 */
	@NonNull
	public ReadRequest merge(final @NonNull DataMerger merger) {
		this.dataMerger = merger;
		return this;
	}

	void notifyValueChanged(final @NonNull BluetoothDevice device, final byte[] value) {
		// With no value callback there is no need for any merging
		if (valueCallback == null)
			return;

		if (dataMerger == null) {
			valueCallback.onDataReceived(device, new Data(value));
		} else {
			if (buffer == null)
				buffer = new ByteArrayOutputStream();
			if (dataMerger.merge(buffer, value, count++)) {
				valueCallback.onDataReceived(device, new Data(buffer.toByteArray()));
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
