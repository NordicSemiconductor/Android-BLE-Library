package no.nordicsemi.android.ble;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.MtuRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class MtuRequest extends Request {
	MtuRequestCallback valueCallback;

	MtuRequest(final Type type, final int mtu) {
		super(type, mtu);
	}

	public MtuRequest then(final MtuRequestCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@Override
	public MtuRequest done(final SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	public MtuRequest fail(final FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
