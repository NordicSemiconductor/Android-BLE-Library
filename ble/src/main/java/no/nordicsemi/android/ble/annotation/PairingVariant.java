package no.nordicsemi.android.ble.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import no.nordicsemi.android.ble.BleManager;

@Retention(RetentionPolicy.SOURCE)
@IntDef(value = {
		BleManager.PAIRING_VARIANT_PIN,
		BleManager.PAIRING_VARIANT_PASSKEY,
		BleManager.PAIRING_VARIANT_PASSKEY_CONFIRMATION,
		BleManager.PAIRING_VARIANT_CONSENT,
		BleManager.PAIRING_VARIANT_DISPLAY_PASSKEY,
		BleManager.PAIRING_VARIANT_DISPLAY_PIN,
		BleManager.PAIRING_VARIANT_OOB_CONSENT
})
public @interface PairingVariant {}