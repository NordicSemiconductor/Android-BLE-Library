package no.nordicsemi.android.ble;

import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.profile.BatteryLevelCallback;

public class BatteryLevelRequest extends Request {
	private BatteryLevelCallback batteryLevelCallback;

	BatteryLevelRequest(final @NonNull Type type) {
		super(type);
	}

	@NonNull
	public Request with(final @NonNull BatteryLevelCallback callback) {
		this.batteryLevelCallback = callback;
		return this;
	}

	void notifyBatteryLevelChanged(final int level) {
		if (batteryLevelCallback != null)
			batteryLevelCallback.onBatteryValueChanged(level);
	}
}
