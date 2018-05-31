package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings("WeakerAccess")
public class ConnectRequest extends Request {

	ConnectRequest(@NonNull final Type type) {
		super(type);
		enqueued = true;
	}

	/**
	 * Use to add a completion callback. The callback will be invoked when the operation has finished
	 * successfully unless {@link #await(int)} or its variant was used, in which case this callback
	 * will be ignored.
	 * <p>
	 * The done callback will also be called when one or more of initialization requests has
	 * failed due to a reason other than disconnect event. This is because
	 * {@link BleManagerCallbacks#onDeviceReady(BluetoothDevice)} is called no matter
	 * if the requests succeeded, or not. Set failure callbacks to initialization requests
	 * to get information about failures.
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	@Override
	public ConnectRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@NonNull
	@Override
	public ConnectRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}
}
