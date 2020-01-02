package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class SetValueRequest extends SimpleRequest {
	private final byte[] data;
	private boolean longReadSupported = true;

	SetValueRequest(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic,
					@Nullable final byte[] data,
					@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		super(type, characteristic);
		this.data = Bytes.copy(data, offset, length);
	}

	SetValueRequest(@NonNull final Type type, @Nullable final BluetoothGattDescriptor descriptor,
					@Nullable final byte[] data,
					@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		super(type, descriptor);
		this.data = Bytes.copy(data, offset, length);
	}

	@NonNull
	@Override
	SetValueRequest setRequestHandler(@NonNull final RequestHandler requestHandler) {
		super.setRequestHandler(requestHandler);
		return this;
	}

	@NonNull
	@Override
	public SetValueRequest setHandler(@NonNull final Handler handler) {
		super.setHandler(handler);
		return this;
	}

	@Override
	@NonNull
	public SetValueRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public SetValueRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@NonNull
	@Override
	public SetValueRequest invalid(@NonNull final InvalidRequestCallback callback) {
		super.invalid(callback);
		return this;
	}

	@Override
	@NonNull
	public SetValueRequest before(@NonNull final BeforeCallback callback) {
		super.before(callback);
		return this;
	}

	/**
	 * Sets whether Long Read procedure is supported by the remote device on the given characteristic
	 * or descriptor. If set to false, the given data will be truncated to match MTU.
	 * Otherwise (default) will only be truncated to fit in the maximum Long Read length, that is
	 * 512 bytes.
	 *
	 * @param longReadSupported whether Long Read procedure is supported on the remote device on
	 *                          the given characteristic or descriptor.
	 * @return The request.
	 */
	@NonNull
	public SetValueRequest allowLongRead(final boolean longReadSupported) {
		this.longReadSupported = longReadSupported;
		return this;
	}

	/**
	 * Returns the data to be assigned to characteristic or descriptor.
	 * If {@link #allowLongRead(boolean)} was called with parameter set to false, the data set will
	 * be truncated to match the MTU. Otherwise, they will be truncated to match the maximum
	 * length of Long-Read procedure, that is 512 bytes.
	 * See Bluetooth Core Specification version 5.1 | Vol 3, Part F, 3.2.9 Long Attribute Values.
	 *
	 * @param mtu the current MTU.
	 * @return The data to be set to the given characteristic or descriptor.
	 */
	byte[] getData(@IntRange(from = 23, to = 517) final int mtu) {
		final int maxLength = longReadSupported ? 512 : mtu - 3;
		if (data.length < maxLength)
			return data;
		return Bytes.copy(data, 0, maxLength);
	}
}
