package no.nordicsemi.android.ble.annotation;

import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		Log.VERBOSE,
		Log.DEBUG,
		Log.INFO,
		Log.WARN,
		Log.ERROR,
		Log.ASSERT,
})
public @interface LogPriority {}
