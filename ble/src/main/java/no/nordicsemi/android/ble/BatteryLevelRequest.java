package no.nordicsemi.android.ble;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.BatteryLevelCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class BatteryLevelRequest extends Request {
	BatteryLevelCallback batteryLevelCallback;

	BatteryLevelRequest(final @NonNull Type type) {
		super(type);
	}

	@NonNull
	public BatteryLevelRequest then(final @Nullable BatteryLevelCallback callback) {
		this.batteryLevelCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public BatteryLevelRequest done(final @Nullable SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	@Override
	@NonNull
	public BatteryLevelRequest fail(final @Nullable FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
