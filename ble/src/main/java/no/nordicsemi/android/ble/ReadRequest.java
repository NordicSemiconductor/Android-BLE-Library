package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.callback.DataCallback;
import no.nordicsemi.android.ble.callback.ValueMerger;

public class ReadRequest extends Request {
	private DataCallback valueCallback;
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
	public Request with(final @NonNull DataCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@NonNull
	public Request with(final @NonNull DataCallback callback, final @NonNull ValueMerger merger) {
		this.valueCallback = callback;
		this.valueMerger = merger;
		return this;
	}

	void notifyValueChanged(final byte[] value) {
		// With no value callback there is no need for any merging
		if (valueCallback == null)
			return;

		if (valueMerger == null) {
			valueCallback.onValueChanged(new Data(value));
		} else {
			if (buffer == null)
				buffer = new ByteArrayOutputStream();
			if (valueMerger.merge(buffer, value, count++)) {
				valueCallback.onValueChanged(new Data(buffer.toByteArray()));
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
