package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Connection request is a request that can be executed during a pending connection without
 * changing its state. Such requests may be added to {@link RequestQueue}.
 */
@SuppressWarnings("WeakerAccess")
public class ConnectionRequest extends Request {

	ConnectionRequest(@NonNull final Type type) {
		super(type);
	}

	ConnectionRequest(@NonNull final Type type,
					  @Nullable final BluetoothGattCharacteristic characteristic) {
		super(type, characteristic);
	}

	ConnectionRequest(@NonNull final Type type,
					  @Nullable final BluetoothGattDescriptor descriptor) {
		super(type, descriptor);
	}
}
