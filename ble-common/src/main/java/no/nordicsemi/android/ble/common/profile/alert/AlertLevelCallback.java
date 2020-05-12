package no.nordicsemi.android.ble.common.profile.alert;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface AlertLevelCallback {
	int ALERT_NONE = 0x00;
	int ALERT_MILD = 0x01;
	int ALERT_HIGH = 0x02;

	/**
	 * Method called when Alert Level characteristic value has changed.
	 *
	 * @param device the target device.
	 * @param level  the new alert level.
	 */
	void onAlertLevelChanged(@NonNull final BluetoothDevice device, @AlertLevel final int level);
}
