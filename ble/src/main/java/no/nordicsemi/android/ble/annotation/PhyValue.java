package no.nordicsemi.android.ble.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import no.nordicsemi.android.ble.callback.PhyCallback;

@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		PhyCallback.PHY_LE_1M,
		PhyCallback.PHY_LE_2M,
		PhyCallback.PHY_LE_CODED
})
public @interface PhyValue {}
