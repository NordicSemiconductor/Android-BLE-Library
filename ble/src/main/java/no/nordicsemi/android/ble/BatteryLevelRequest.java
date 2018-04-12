package no.nordicsemi.android.ble;

import no.nordicsemi.android.ble.callback.BatteryLevelCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

public class BatteryLevelRequest extends Request {
	BatteryLevelCallback batteryLevelCallback;

	BatteryLevelRequest(final Type type) {
		super(type);
	}

	public BatteryLevelRequest then(final BatteryLevelCallback callback) {
		this.batteryLevelCallback = callback;
		return this;
	}

	public BatteryLevelRequest done(final SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	public BatteryLevelRequest fail(final FailCallback callback) {
		this.failCallback = callback;
		return this;
	}
}
