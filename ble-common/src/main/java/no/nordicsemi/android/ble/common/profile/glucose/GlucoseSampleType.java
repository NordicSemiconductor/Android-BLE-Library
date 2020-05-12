package no.nordicsemi.android.ble.common.profile.glucose;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		GlucoseTypes.TYPE_CAPILLARY_WHOLE_BLOOD,
		GlucoseTypes.TYPE_CAPILLARY_PLASMA,
		GlucoseTypes.TYPE_VENOUS_WHOLE_BLOOD,
		GlucoseTypes.TYPE_VENOUS_PLASMA,
		GlucoseTypes.TYPE_ARTERIAL_WHOLE_BLOOD,
		GlucoseTypes.TYPE_ARTERIAL_PLASMA,
		GlucoseTypes.TYPE_UNDETERMINED_WHOLE_BLOOD,
		GlucoseTypes.TYPE_UNDETERMINED_PLASMA,
		GlucoseTypes.TYPE_INTERSTITIAL_FLUID_ISF,
		GlucoseTypes.TYPE_CONTROL_SOLUTION
})
public @interface GlucoseSampleType {}