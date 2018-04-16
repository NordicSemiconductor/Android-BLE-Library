package no.nordicsemi.android.ble.callback.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.profile.DateTimeCallback;
import no.nordicsemi.android.ble.profile.ProfileDataCallback;

@SuppressWarnings({"ConstantConditions", "WeakerAccess", "unused"})
public abstract class DateTimeDataCallback implements ProfileDataCallback, DateTimeCallback {

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		final Calendar calendar = readDateTime(data, 0);
		if (calendar == null) {
			onInvalidDataReceived(device, data);
			return;
		}
		onDateTimeReceived(device, calendar);
	}

	@Override
	public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		// ignore
	}

	static Calendar readDateTime(@NonNull final Data data, final int offset) {
		if (data.size() < offset + 7)
			return null;

		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, data.getIntValue(Data.FORMAT_UINT16, offset));
		calendar.set(Calendar.MONTH, data.getIntValue(Data.FORMAT_UINT8, offset + 2) - 1); // months are 1-based
		calendar.set(Calendar.DAY_OF_MONTH, data.getIntValue(Data.FORMAT_UINT8, offset + 3));
		calendar.set(Calendar.HOUR_OF_DAY, data.getIntValue(Data.FORMAT_UINT8, offset + 4));
		calendar.set(Calendar.MINUTE, data.getIntValue(Data.FORMAT_UINT8, offset + 5));
		calendar.set(Calendar.SECOND, data.getIntValue(Data.FORMAT_UINT8, offset + 6));
		return calendar;
	}
}
