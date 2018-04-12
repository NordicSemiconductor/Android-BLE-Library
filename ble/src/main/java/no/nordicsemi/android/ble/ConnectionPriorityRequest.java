package no.nordicsemi.android.ble;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import no.nordicsemi.android.ble.callback.ConnectionPriorityCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class ConnectionPriorityRequest extends Request {
	ConnectionPriorityCallback valueCallback;

	ConnectionPriorityRequest(final @NonNull Type type, final int priority) {
		super(type, priority);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	@NonNull
	public ConnectionPriorityRequest then(final @Nullable ConnectionPriorityCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public ConnectionPriorityRequest done(final @Nullable SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public ConnectionPriorityRequest fail(final @Nullable FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
