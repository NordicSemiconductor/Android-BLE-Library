package no.nordicsemi.android.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import java.util.Calendar;

public interface DateTimeCallback {

	/**
	 * Callback called when datetime packet has been received.
	 * @param device target device.
	 * @param calendar date and time received, as {@link Calendar} object.
	 */
	void onDateTimeReceived(@NonNull final BluetoothDevice device, @NonNull final Calendar calendar);
}
