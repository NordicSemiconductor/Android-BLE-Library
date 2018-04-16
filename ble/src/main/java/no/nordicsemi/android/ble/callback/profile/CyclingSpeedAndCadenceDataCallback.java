package no.nordicsemi.android.ble.callback.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import no.nordicsemi.android.ble.callback.Data;
import no.nordicsemi.android.ble.profile.CyclingSpeedAndCadenceCallback;
import no.nordicsemi.android.ble.profile.ProfileDataCallback;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class CyclingSpeedAndCadenceDataCallback implements ProfileDataCallback, CyclingSpeedAndCadenceCallback {
	private static final byte WHEEL_REVOLUTIONS_DATA_PRESENT = 0x01; // 1 bit
	private static final byte CRANK_REVOLUTION_DATA_PRESENT = 0x02; // 1 bit

	private long mInitialWheelRevolutions = -1;
	private long mLastWheelRevolutions = -1;
	private int mLastWheelEventTime = -1;
	private int mLastCrankRevolutions = -1;
	private int mLastCrankEventTime = -1;
	private float mWheelCadence = -1;

	@SuppressWarnings("ConstantConditions")
	@Override
	public void onDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
		if (data.size() < 1) {
			onInvalidDataReceived(device, data);
			return;
		}

		// Decode the new data
		int offset = 0;
		final int flags = data.getByte(offset);
		offset += 1;

		final boolean wheelRevPresent = (flags & WHEEL_REVOLUTIONS_DATA_PRESENT) > 0;
		final boolean crankRevPreset = (flags & CRANK_REVOLUTION_DATA_PRESENT) > 0;

		if (data.size() < 1 + (wheelRevPresent ? 6 : 0) + (crankRevPreset ? 4 : 0)) {
			onInvalidDataReceived(device, data);
			return;
		}

		if (wheelRevPresent) {
			final long wheelRevolutions = data.getIntValue(Data.FORMAT_UINT32, offset) & 0xFFFFFFFFL;
			offset += 4;

			final int lastWheelEventTime = data.getIntValue(Data.FORMAT_UINT16, offset); // 1/1024 s
			offset += 2;

			if (mInitialWheelRevolutions < 0)
				mInitialWheelRevolutions = wheelRevolutions;

			// Notify listener about the new measurement
			onWheelMeasurementReceived(device, wheelRevolutions, lastWheelEventTime);
		}

		if (crankRevPreset) {
			final int crankRevolutions = data.getIntValue(Data.FORMAT_UINT16, offset);
			offset += 2;

			final int lastCrankEventTime = data.getIntValue(Data.FORMAT_UINT16, offset);
			// offset += 2;

			// Notify listener about the new measurement
			onCrankMeasurementReceived(device, crankRevolutions, lastCrankEventTime);
		}
	}

	@Override
	public void onInvalidDataReceived(@NonNull final BluetoothDevice device, final @NonNull Data data) {
		// ignore
	}

	/**
	 * Method called when the data received had wheel revolution data present.
	 * The default implementation calculates the total distance, distance since connection and
	 * current speed and calls {@link CyclingSpeedAndCadenceCallback#onDistanceChanged(BluetoothDevice, float, float, float)}.
	 *
	 * @param device target device.
	 * @param wheelRevolutions cumulative wheel revolutions since the CSC device was reset (UINT32).
	 * @param lastWheelEventTime last wheel event time in 1/1024 s (UINT16).
	 */
	protected void onWheelMeasurementReceived(final @NonNull BluetoothDevice device, final long wheelRevolutions, final int lastWheelEventTime) {
		if (mLastWheelEventTime == lastWheelEventTime)
			return;

		if (mLastWheelRevolutions >= 0) {
			final float circumference = getWheelCircumference();

			float timeDifference;
			if (lastWheelEventTime < mLastWheelEventTime)
				timeDifference = (65535 + lastWheelEventTime - mLastWheelEventTime) / 1024.0f; // [s]
			else
				timeDifference = (lastWheelEventTime - mLastWheelEventTime) / 1024.0f; // [s]
			final float distanceDifference = (wheelRevolutions - mLastWheelRevolutions) * circumference / 1000.0f; // [m]
			final float totalDistance = (float) wheelRevolutions * circumference / 1000.0f; // [m]
			final float distance = (float) (wheelRevolutions - mInitialWheelRevolutions) * circumference / 1000.0f; // [m]
			final float speed = distanceDifference / timeDifference; // [m/s]
			mWheelCadence = (wheelRevolutions - mLastWheelRevolutions) * 60.0f / timeDifference; // [revolutions/s]

			// Notify listener about the new measurement
			onDistanceChanged(device, totalDistance, distance, speed);
		}
		mLastWheelRevolutions = wheelRevolutions;
		mLastWheelEventTime = lastWheelEventTime;
	}

	/**
	 * Method called when the data received had crank revolution data present.
	 * The default implementation calculates the crank cadence and gear ratio and
	 * calls {@link CyclingSpeedAndCadenceCallback#onCrankDataChanged(BluetoothDevice, float, float)}
	 *
	 * @param device target device.
	 * @param crankRevolutions cumulative crank revolutions since the CSC device was reset (UINT16).
	 * @param lastCrankEventTime last crank event time in 1/1024 s (UINT16).
	 */
	protected void onCrankMeasurementReceived(final @NonNull BluetoothDevice device, final int crankRevolutions, final int lastCrankEventTime) {
		if (mLastCrankEventTime == lastCrankEventTime)
			return;

		if (mLastCrankRevolutions >= 0) {
			float timeDifference;
			if (lastCrankEventTime < mLastCrankEventTime)
				timeDifference = (65535 + lastCrankEventTime - mLastCrankEventTime) / 1024.0f; // [s]
			else
				timeDifference = (lastCrankEventTime - mLastCrankEventTime) / 1024.0f; // [s]

			final float crankCadence = (crankRevolutions - mLastCrankRevolutions) * 60.0f / timeDifference; // [revolutions/s]
			if (crankCadence > 0) {
				final float gearRatio = mWheelCadence / crankCadence;

				// Notify listener about the new measurement
				onCrankDataChanged(device, crankCadence, gearRatio);
			} else {

				// Notify listener about the new measurement
				onCrankDataChanged(device, 0, 0);
			}
		}
		mLastCrankRevolutions = crankRevolutions;
		mLastCrankEventTime = lastCrankEventTime;
	}
}
