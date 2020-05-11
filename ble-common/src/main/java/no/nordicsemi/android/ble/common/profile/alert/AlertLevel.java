package no.nordicsemi.android.ble.common.profile.alert;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		AlertLevelCallback.ALERT_NONE,
		AlertLevelCallback.ALERT_MILD,
		AlertLevelCallback.ALERT_HIGH
})
public @interface AlertLevel {}