package no.nordicsemi.android.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

public interface CyclingSpeedAndCadenceCallback {
	float WHEEL_CIRCUMFERENCE_DEFAULT = 2340; // [mm]


	/**
	 * This method should return the wheel circumference in millimeters.
	 * See http://www.bikecalc.com/wheel_size_math for values.
	 *
	 * @return wheel circumference in mm. By default it returns {@link #WHEEL_CIRCUMFERENCE_DEFAULT}.
	 */
	default float getWheelCircumference() {
		return WHEEL_CIRCUMFERENCE_DEFAULT;
	}

	/**
	 * Callback called when the traveled distance and speed has changed.
	 * The distance and speed calculations are based on the wheel circumference obtained
	 * with {@link #getWheelCircumference()}. Make sure it returns the correct value.
	 *
	 * @param device target device.
	 * @param totalDistance total distance traveled since the measuring device was reset, in meters.
	 * @param distance distance traveled since the phone connected to the CSC device, in meters.
	 * @param speed current speed, in meters per second.
	 */
	void onDistanceChanged(@NonNull final BluetoothDevice device, final float totalDistance, final float distance, final float speed);

	/**
	 * Callback called when the crank data (cadence or gear ratio) has changed.
	 *
	 * @param device target device.
	 * @param crankCadence new crank cadence.
	 * @param gearRatio new gear ratio.
	 */
	void onCrankDataChanged(@NonNull final BluetoothDevice device, final float crankCadence, final float gearRatio);
}
