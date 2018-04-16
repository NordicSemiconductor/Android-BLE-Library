package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.callback.DataCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.DataMerger;

public class ReadRequest extends Request {
	private DataCallback valueCallback;
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
	public ReadRequest with(final @NonNull DataCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@NonNull
	public ReadRequest with(final @NonNull DataCallback callback, final @NonNull DataMerger merger) {
		this.valueCallback = callback;
		this.dataMerger = merger;
		return this;
	}

	void notifyValueChanged(final byte[] value) {
		// With no value callback there is no need for any merging
		if (valueCallback == null)
			return;

		if (dataMerger == null) {
			valueCallback.onDataReceived(new Data(value));
		} else {
			if (buffer == null)
				buffer = new ByteArrayOutputStream();
			if (dataMerger.merge(buffer, value, count++)) {
				valueCallback.onDataReceived(new Data(buffer.toByteArray()));
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
