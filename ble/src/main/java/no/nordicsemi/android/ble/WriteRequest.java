package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import no.nordicsemi.android.ble.callback.DefaultMtuSpitter;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.ValueSplitter;

public class WriteRequest extends Request {
	private final static ValueSplitter MTU_SPLITTER = new DefaultMtuSpitter();
	ValueSplitter valueSplitter;

	WriteRequest(final Type type, final BluetoothGattCharacteristic characteristic, final int writeType, final byte[] data, final int offset, final int length) {
		super(type, characteristic, writeType, data, offset, length);
	}

	WriteRequest(final Type type, final BluetoothGattDescriptor descriptor, final byte[] data, final int offset, final int length) {
		super(type, descriptor, data, offset, length);
	}

	public WriteRequest split(final ValueSplitter splitter) {
		this.valueSplitter = splitter;
		return this;
	}

	public WriteRequest split() {
		this.valueSplitter = MTU_SPLITTER;
		return this;
	}

	@Override
	public WriteRequest done(final SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	public WriteRequest fail(final FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
