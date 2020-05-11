package no.nordicsemi.android.ble.common.profile.hr;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		BodySensorLocationCallback.SENSOR_LOCATION_OTHER,
		BodySensorLocationCallback.SENSOR_LOCATION_CHEST,
		BodySensorLocationCallback.SENSOR_LOCATION_WRIST,
		BodySensorLocationCallback.SENSOR_LOCATION_FINGER,
		BodySensorLocationCallback.SENSOR_LOCATION_HAND,
		BodySensorLocationCallback.SENSOR_LOCATION_EAR_LOBE,
		BodySensorLocationCallback.SENSOR_LOCATION_FOOT,
})
public @interface BodySensorLocation {}