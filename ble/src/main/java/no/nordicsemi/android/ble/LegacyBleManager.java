package no.nordicsemi.android.ble;

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

/**
 * When migrating from BLE Manager 2.1.1 to 2.2.0, the BleManager is no longer a template class.
 * Callbacks need to be passed in other way, e.g. using LiveData, RxJava or with a callback,
 * but the BleManager will not hold the callbacks reference.
 *
 * To make the migration easier, this class behaves the same way as the old BleManager.
 * Replace the base class of your manager to LegacyBleManager.
 * @param <E> the callbacks interface.
 */
@SuppressWarnings({"WeakerAccess", "unused", "deprecation"})
@Deprecated
public abstract class LegacyBleManager<E extends BleManagerCallbacks> extends BleManager {
    protected E mCallbacks;

    public LegacyBleManager(@NonNull final Context context) {
        super(context);
    }

    public LegacyBleManager(@NonNull final Context context, @NonNull final Handler handler) {
        super(context, handler);
    }

    @Override
    public void setGattCallbacks(@NonNull final BleManagerCallbacks callbacks) {
        super.setGattCallbacks(callbacks);
        //noinspection unchecked
        mCallbacks = (E) callbacks;
    }
}
