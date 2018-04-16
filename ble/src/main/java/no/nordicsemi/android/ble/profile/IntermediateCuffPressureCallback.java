package no.nordicsemi.android.ble.profile;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

@SuppressWarnings("unused")
public interface IntermediateCuffPressureCallback {
	int UNIT_mmHG = 0;
	int UNIT_kPa = 1;

	class Status {
		boolean bodyMovementDetected;
		boolean cuffTooLose;
		boolean irregularPulseDetected;
		boolean pulseRateInRange;
		boolean pulseRateExceedsUpperLimit;
		boolean pulseRateLessThenLowerLimit;
		boolean improperMeasurementPosition;

		public Status(final int status) {
			bodyMovementDetected = (status & 0x01) != 0;
			cuffTooLose = (status & 0x02) != 0;
			irregularPulseDetected = (status & 0x04) != 0;
			pulseRateInRange = (status & 0x18) >> 3 == 0;
			pulseRateExceedsUpperLimit = (status & 0x18) >> 3 == 1;
			pulseRateLessThenLowerLimit = (status & 0x18) >> 3 == 2;
			improperMeasurementPosition = (status & 0x20) != 0;
		}
	}

	/**
	 * Callback called when Intermediate Cuff Pressure packet has been received.
	 * @param device target device.
	 * @param cuffPressure cuff pressure.
	 * @param unit measurement unit, one of {@link #UNIT_mmHG} or {@link #UNIT_kPa}.
	 * @param pulseRate optional pulse rate in beats per minute.
	 * @param userID optional user ID. Value 255 means 'unknown user'.
	 * @param status optional measurement status.
	 * @param calendar optional measurement timestamp.
	 */
	void onIntermediateCuffPressureReceived(final @NonNull BluetoothDevice device, final float cuffPressure, final int unit,
											final @Nullable Float pulseRate, final @Nullable Integer userID,
											final @Nullable Status status, final @Nullable Calendar calendar);
}
