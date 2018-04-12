package no.nordicsemi.android.ble;

import no.nordicsemi.android.ble.callback.ConnectionPriorityRequestCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class ConnectionPriorityRequest extends Request {
	ConnectionPriorityRequestCallback valueCallback;

	ConnectionPriorityRequest(final Type type, final int priority) {
		super(type, priority);
	}

	public ConnectionPriorityRequest then(final ConnectionPriorityRequestCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@Override
	public ConnectionPriorityRequest done(final SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	public ConnectionPriorityRequest fail(final FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
