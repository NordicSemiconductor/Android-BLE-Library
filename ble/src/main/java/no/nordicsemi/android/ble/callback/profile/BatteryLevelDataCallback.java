package no.nordicsemi.android.ble.callback.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.profile.BatteryLevelCallback;
import no.nordicsemi.android.ble.profile.ProfileDataCallback;

@SuppressWarnings("WeakerAccess")
public abstract class BatteryLevelDataCallback implements ProfileDataCallback, BatteryLevelCallback {

	@SuppressWarnings("ConstantConditions")
	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
		if (data.size() == 1) {
			final int batteryLevel = data.getByte(0);
			onBatteryLevelChanged(device, batteryLevel);
		} else {
			onInvalidDataReceived(device, data);
		}
	}
}
