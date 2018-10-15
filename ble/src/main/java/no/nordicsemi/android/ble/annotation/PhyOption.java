package no.nordicsemi.android.ble.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import no.nordicsemi.android.ble.PhyRequest;

@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		PhyRequest.PHY_OPTION_NO_PREFERRED,
		PhyRequest.PHY_OPTION_S2,
		PhyRequest.PHY_OPTION_S8
})
public @interface PhyOption {}