package no.nordicsemi.android.ble.common.profile.sc;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		SensorLocationTypes.SENSOR_LOCATION_OTHER,
		SensorLocationTypes.SENSOR_LOCATION_TOP_OF_SHOE,
		SensorLocationTypes.SENSOR_LOCATION_IN_SHOE,
		SensorLocationTypes.SENSOR_LOCATION_HIP,
		SensorLocationTypes.SENSOR_LOCATION_FRONT_WHEEL,
		SensorLocationTypes.SENSOR_LOCATION_LEFT_CRANK,
		SensorLocationTypes.SENSOR_LOCATION_RIGHT_CRANK,
		SensorLocationTypes.SENSOR_LOCATION_LEFT_PEDAL,
		SensorLocationTypes.SENSOR_LOCATION_RIGHT_PEDAL,
		SensorLocationTypes.SENSOR_LOCATION_FRONT_HUB,
		SensorLocationTypes.SENSOR_LOCATION_REAR_DROPOUT,
		SensorLocationTypes.SENSOR_LOCATION_CHAINSTAY,
		SensorLocationTypes.SENSOR_LOCATION_REAR_WHEEL,
		SensorLocationTypes.SENSOR_LOCATION_REAR_HUB,
		SensorLocationTypes.SENSOR_LOCATION_CHEST,
		SensorLocationTypes.SENSOR_LOCATION_SPIDER,
		SensorLocationTypes.SENSOR_LOCATION_CHAIN_RING
})
public @interface SensorLocation {}