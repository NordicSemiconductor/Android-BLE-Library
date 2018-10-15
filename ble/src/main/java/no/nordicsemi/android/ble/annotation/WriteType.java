package no.nordicsemi.android.ble.annotation;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT,
		BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
		BluetoothGattCharacteristic.WRITE_TYPE_SIGNED,
})
public @interface WriteType {}