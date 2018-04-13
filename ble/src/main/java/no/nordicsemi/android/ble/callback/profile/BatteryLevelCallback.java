package no.nordicsemi.android.ble.callback.profile;

public interface BatteryLevelCallback {

	/**
	 * Callback received each time the Battery Level value was read or has changed using notifications or indications.
	 * @param batteryLevel the battery value in percent
	 */
	void onBatteryValueChanged(final int batteryLevel);
}
