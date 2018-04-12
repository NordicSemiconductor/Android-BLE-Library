package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.ValueCallback;
import no.nordicsemi.android.ble.callback.ValueMerger;

public class ReadRequest extends Request {
	ValueCallback valueCallback;
	ValueMerger valueMerger;

	ReadRequest(final @NonNull Type type, final @NonNull BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ReadRequest(final @NonNull Type type, final @NonNull BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	@NonNull
	public ReadRequest then(final @Nullable ValueCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@NonNull
	public ReadRequest merge(final @Nullable ValueMerger merger) {
		this.valueMerger = merger;
		return this;
	}

	@Override
	@NonNull
	public ReadRequest done(final @Nullable SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public ReadRequest fail(final @Nullable FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
