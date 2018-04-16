package no.nordicsemi.android.ble.callback.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import java.util.Calendar;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.profile.BloodPressureMeasurementCallback;
import no.nordicsemi.android.ble.profile.ProfileDataCallback;

@SuppressWarnings("ConstantConditions")
public abstract class BloodPressureMeasurementDataCallback implements ProfileDataCallback, BloodPressureMeasurementCallback {

	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
		if (data.size() < 7) {
			onInvalidDataReceived(device, data);
			return;
		}
		// First byte: flags
		int offset = 0;
		final int flags = data.getIntValue(Data.FORMAT_UINT8, offset++);

		// See UNIT_* for unit options
		final int unit = flags & 0x01;
		final boolean timestampPresent = (flags & 0x02) > 0;
		final boolean pulseRatePresent = (flags & 0x04) > 0;
		final boolean userIdPresent = (flags & 0x08) > 0;
		final boolean measurementStatusPresent = (flags & 0x10) > 0;

		if (data.size() < 7
				+ (timestampPresent ? 7 : 0) + (pulseRatePresent ? 2 : 0)
				+ (userIdPresent ? 1 : 0) + (measurementStatusPresent ? 2 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Following bytes - systolic, diastolic and mean arterial pressure
		final float systolic = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
		final float diastolic = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 2);
		final float meanArterialPressure = data.getFloatValue(Data.FORMAT_SFLOAT, offset + 4);
		offset += 6;

		// Parse timestamp if present
		Calendar calendar = null;
		if (timestampPresent) {
			calendar = DateTimeDataCallback.readDateTime(data, offset);
			offset += 7;
		}

		// Parse pulse rate if present
		Float pulseRate = null;
		if (pulseRatePresent) {
			pulseRate = data.getFloatValue(Data.FORMAT_SFLOAT, offset);
			offset += 2;
		}

		// Read user id if present
		Integer userId = null;
		if (userIdPresent) {
			userId = data.getIntValue(Data.FORMAT_UINT8, offset);
			offset += 1;
		}

		// Read measurement status if present
		Status status = null;
		if (measurementStatusPresent) {
			final int measurementStatus = data.getIntValue(Data.FORMAT_UINT16, offset);
			// offset += 2;
			status = new Status(measurementStatus);
		}

		onBloodPressureMeasurementReceived(device, systolic, diastolic, meanArterialPressure, unit, pulseRate, userId, status, calendar);
	}
}
