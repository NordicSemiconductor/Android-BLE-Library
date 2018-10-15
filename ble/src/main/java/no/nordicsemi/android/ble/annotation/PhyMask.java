package no.nordicsemi.android.ble.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import no.nordicsemi.android.ble.PhyRequest;

@Retention(RetentionPolicy.SOURCE)
@IntDef(flag = true, value = {
		PhyRequest.PHY_LE_1M_MASK,
		PhyRequest.PHY_LE_2M_MASK,
		PhyRequest.PHY_LE_CODED_MASK
})
public @interface PhyMask {}
