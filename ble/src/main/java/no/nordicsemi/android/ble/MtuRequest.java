package no.nordicsemi.android.ble;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.MtuCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class MtuRequest extends Request {
	private MtuCallback valueCallback;
	private final int value;

	MtuRequest(final @NonNull Type type, int mtu) {
		super(type);
		if (mtu < 23)
			mtu = 23;
		if (mtu > 517)
			mtu = 517;
		this.value = mtu;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public MtuRequest then(final @NonNull MtuCallback callback) {
		this.valueCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public MtuRequest done(final @NonNull SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public MtuRequest fail(final @NonNull FailCallback callback) {
		super.fail(callback);
		return this;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	void notifyMtuChanged(final int mtu) {
		if (valueCallback != null)
			valueCallback.onMtuChanged(mtu);
	}

	int getRequiredMtu() {
		return value;
	}
}
