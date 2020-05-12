package no.nordicsemi.android.ble.common.profile.ht;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		HealthThermometerTypes.TYPE_ARMPIT,
		HealthThermometerTypes.TYPE_BODY,
		HealthThermometerTypes.TYPE_EAR,
		HealthThermometerTypes.TYPE_FINGER,
		HealthThermometerTypes.TYPE_GASTRO_INTESTINAL_TRACT,
		HealthThermometerTypes.TYPE_MOUTH,
		HealthThermometerTypes.TYPE_RECTUM,
		HealthThermometerTypes.TYPE_TOE,
		HealthThermometerTypes.TYPE_TYMPANUM
})
public @interface TemperatureType {}