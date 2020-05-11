package no.nordicsemi.android.ble.common.profile.glucose;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		GlucoseTypes.SAMPLE_LOCATION_FINGER,
		GlucoseTypes.SAMPLE_LOCATION_ALTERNATE_SITE_TEST,
		GlucoseTypes.SAMPLE_LOCATION_EARLOBE,
		GlucoseTypes.SAMPLE_LOCATION_CONTROL_SOLUTION,
		GlucoseTypes.SAMPLE_LOCATION_SUBCUTANEOUS_TISSUE,
		GlucoseTypes.SAMPLE_LOCATION_VALUE_NOT_AVAILABLE,
})
public @interface GlucoseSampleLocation {}