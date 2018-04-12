package no.nordicsemi.android.ble;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.MtuCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class MtuRequest extends Request {
	MtuCallback valueCallback;

	MtuRequest(final @NonNull Type type, final int mtu) {
		super(type, mtu);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public MtuRequest then(final @Nullable MtuCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public MtuRequest done(final @Nullable SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public MtuRequest fail(final @Nullable FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
