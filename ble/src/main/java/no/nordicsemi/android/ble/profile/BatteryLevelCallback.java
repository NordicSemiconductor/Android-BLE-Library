package no.nordicsemi.android.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

public interface BatteryLevelCallback {

	/**
	 * Callback received each time the Battery Level value was read or has changed using notifications or indications.
	 * @param device target device.
	 * @param batteryLevel the battery value in percent.
	 */
	void onBatteryLevelChanged(@NonNull final BluetoothDevice device, final int batteryLevel);
}
