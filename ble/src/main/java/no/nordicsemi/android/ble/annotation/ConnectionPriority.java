package no.nordicsemi.android.ble.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import no.nordicsemi.android.ble.ConnectionPriorityRequest;

@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		ConnectionPriorityRequest.CONNECTION_PRIORITY_BALANCED,
		ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH,
		ConnectionPriorityRequest.CONNECTION_PRIORITY_LOW_POWER
})
public @interface ConnectionPriority {}