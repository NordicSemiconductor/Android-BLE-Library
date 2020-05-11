package no.nordicsemi.android.ble.common.profile.glucose;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		GlucoseMeasurementCallback.UNIT_kg_L,
		GlucoseMeasurementCallback.UNIT_mol_L
})
public @interface GlucoseUnit {}