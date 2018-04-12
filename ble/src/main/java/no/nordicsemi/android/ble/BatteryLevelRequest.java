package no.nordicsemi.android.ble;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.BatteryLevelCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class BatteryLevelRequest extends Request {
	private BatteryLevelCallback batteryLevelCallback;

	BatteryLevelRequest(final @NonNull Type type) {
		super(type);
	}

	@NonNull
	public BatteryLevelRequest then(final @NonNull BatteryLevelCallback callback) {
		this.batteryLevelCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public BatteryLevelRequest done(final @NonNull SuccessCallback callback) {
		super.done(callback);
		return this;
	}

	@Override
	@NonNull
	public BatteryLevelRequest fail(final @NonNull FailCallback callback) {
		super.fail(callback);
		return this;
	}

	void notifyBatteryLevelChanged(final int level) {
		if (batteryLevelCallback != null)
			batteryLevelCallback.onBatteryValueChanged(level);
	}
}
