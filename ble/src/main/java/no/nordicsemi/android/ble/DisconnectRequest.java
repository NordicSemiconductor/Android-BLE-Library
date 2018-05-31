package no.nordicsemi.android.ble;

import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

@SuppressWarnings("WeakerAccess")
public class DisconnectRequest extends Request {

	DisconnectRequest(@NonNull final Type type) {
		super(type);
		enqueued = true;
	}

	@NonNull
	@Override
	public DisconnectRequest done(@NonNull final SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@NonNull
	@Override
	public DisconnectRequest fail(@NonNull final FailCallback callback) {
		super.fail(callback);
		return this;
	}
}
