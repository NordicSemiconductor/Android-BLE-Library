package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.ValueCallback;
import no.nordicsemi.android.ble.callback.ValueMerger;

public class ReadRequest extends Request {
	ValueCallback valueCallback;
	ValueMerger valueMerger;

	ReadRequest(final Type type, final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ReadRequest(final Type type, final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}

	public ReadRequest then(final ValueCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	public ReadRequest merge(final ValueMerger merger) {
		this.valueMerger = merger;
		return this;
	}

	@Override
	public ReadRequest done(final SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	public ReadRequest fail(final FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
