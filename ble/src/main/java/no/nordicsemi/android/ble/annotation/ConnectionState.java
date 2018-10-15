package no.nordicsemi.android.ble.annotation;

import android.bluetooth.BluetoothProfile;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		BluetoothProfile.STATE_DISCONNECTED,
		BluetoothProfile.STATE_CONNECTING,
		BluetoothProfile.STATE_CONNECTED,
		BluetoothProfile.STATE_DISCONNECTING,
})
public @interface ConnectionState {}