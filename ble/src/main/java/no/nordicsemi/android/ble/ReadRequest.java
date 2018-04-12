package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.ValueCallback;
import no.nordicsemi.android.ble.callback.ValueMerger;

public class ReadRequest extends Request {
	private ValueCallback valueCallback;
	private ValueMerger valueMerger;
	private ByteArrayOutputStream buffer;
	private int count = 0;

	ReadRequest(final @NonNull Type type, final @NonNull BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ReadRequest(final @NonNull Type type, final @NonNull BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	@NonNull
	public ReadRequest then(final @NonNull ValueCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@NonNull
	public ReadRequest then(final @NonNull ValueCallback callback, final @NonNull ValueMerger merger) {
		this.valueCallback = callback;
		this.valueMerger = merger;
		return this;
	}

	@Override
	@NonNull
	public ReadRequest done(final @NonNull SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public ReadRequest fail(final @NonNull FailCallback callback) {
		super.fail(callback);
		return this;
	}

	void notifyValueChanged(final byte[] value) {
		// With no value callback there is no need for any merging
		if (valueCallback == null)
			return;

		if (valueMerger == null) {
			valueCallback.onValueChanged(value);
		} else {
			if (buffer == null)
				buffer = new ByteArrayOutputStream();
			if (valueMerger.merge(buffer, value, count++)) {
				valueCallback.onValueChanged(buffer.toByteArray());
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
