package no.nordicsemi.android.ble.observer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import androidx.annotation.NonNull;

public interface BondingObserver {
	/**
	 * Called when an {@link BluetoothGatt#GATT_INSUFFICIENT_AUTHENTICATION} error occurred and the
	 * device bond state is {@link BluetoothDevice#BOND_NONE}.
	 *
	 * @param device the device that requires bonding.
	 */
	void onBondingRequired(@NonNull final BluetoothDevice device);

	/**
	 * Called when the device has been successfully bonded.
	 *
	 * @param device the device that got bonded.
	 */
	void onBonded(@NonNull final BluetoothDevice device);

	/**
	 * Called when the bond state has changed from {@link BluetoothDevice#BOND_BONDING} to
	 * {@link BluetoothDevice#BOND_NONE}.
	 *
	 * @param device the device that failed to bond.
	 */
	void onBondingFailed(@NonNull final BluetoothDevice device);
}
