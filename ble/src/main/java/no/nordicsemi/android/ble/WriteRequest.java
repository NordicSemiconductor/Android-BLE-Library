package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.DefaultMtuSpitter;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.callback.ValueSplitter;

public class WriteRequest extends Request {
	private final static ValueSplitter MTU_SPLITTER = new DefaultMtuSpitter();
	ValueSplitter valueSplitter;

	WriteRequest(final @NonNull Type type, final @NonNull BluetoothGattCharacteristic characteristic, final int writeType, final @NonNull byte[] data, final int offset, final int length) {
		super(type, characteristic, writeType, data, offset, length);
	}

	WriteRequest(final @NonNull Type type, final @NonNull BluetoothGattDescriptor descriptor, final @NonNull byte[] data, final int offset, final int length) {
		super(type, descriptor, data, offset, length);
	}

	@NonNull
	public WriteRequest split(final @Nullable ValueSplitter splitter) {
		this.valueSplitter = splitter;
		return this;
	}

	@NonNull
	public WriteRequest split() {
		this.valueSplitter = MTU_SPLITTER;
		return this;
	}

	@Override
	@NonNull
	public WriteRequest done(final @Nullable SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public WriteRequest fail(final @Nullable FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
