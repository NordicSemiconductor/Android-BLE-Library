package no.nordicsemi.android.ble.callback.profile;

import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.Data;

@SuppressWarnings("WeakerAccess")
public abstract class BatteryLevelCallback implements ProfileDataCallback {

	@SuppressWarnings("ConstantConditions")
	@Override
	public void onDataReceived(final @NonNull Data data) {
		if (data.size() == 1) {
			final int batteryLevel = data.getByte(0);
			onBatteryValueChanged(batteryLevel);
		} else {
			onInvalidDataReceived(data);
		}
	}

	@Override
	public void onInvalidDataReceived(final @NonNull Data data) {
		// ignore
	}

	/**
	 * Callback received each time the Battery Level value was read or has changed using notifications or indications.
	 * @param batteryLevel the battery value in percent
	 */
	public void onBatteryValueChanged(final int batteryLevel) {
		// empty default implementation
	}
}
