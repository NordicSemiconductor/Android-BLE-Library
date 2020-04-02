/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.observer.BondingObserver;
import no.nordicsemi.android.ble.observer.ConnectionObserver;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

/**
 * The BleManagerCallbacks should be overridden in your app and all the 'high level' callbacks
 * should be added there.
 *
 * @deprecated Use per-request callbacks instead. Check out deprecation descriptions for methods
 * below for details.
 */
@SuppressWarnings({"DeprecatedIsStillUsed", "unused"})
@Deprecated
public interface BleManagerCallbacks {

	/**
	 * Called when the Android device started connecting to given device.
	 * The {@link #onDeviceConnected(BluetoothDevice)} will be called when the device is connected,
	 * or {@link #onError(BluetoothDevice, String, int)} in case of error.
	 *
	 * @param device the device that got connected.
	 * @deprecated Use {@link ConnectionObserver#onDeviceConnecting(BluetoothDevice)} instead.
	 */
	@Deprecated
	void onDeviceConnecting(@NonNull final BluetoothDevice device);

	/**
	 * Called when the device has been connected. This does not mean that the application may start
	 * communication.
	 * A service discovery will be handled automatically after this call. Service discovery
	 * may ends up with calling {@link #onServicesDiscovered(BluetoothDevice, boolean)} or
	 * {@link #onDeviceNotSupported(BluetoothDevice)} if required services have not been found.
	 *
	 * @param device the device that got connected.
	 * @deprecated Use {@link ConnectionObserver#onDeviceConnected(BluetoothDevice)} instead.
	 */
	@Deprecated
	void onDeviceConnected(@NonNull final BluetoothDevice device);

	/**
	 * Called when user initialized disconnection.
	 *
	 * @param device the device that gets disconnecting.
	 * @deprecated Use {@link ConnectionObserver#onDeviceDisconnecting(BluetoothDevice)} instead.
	 */
	@Deprecated
	void onDeviceDisconnecting(@NonNull final BluetoothDevice device);

	/**
	 * Called when the device has disconnected (when the callback returned
	 * {@link BluetoothGattCallback#onConnectionStateChange(BluetoothGatt, int, int)} with state
	 * DISCONNECTED), but ONLY if the {@link ConnectRequest#shouldAutoConnect()} method returned
	 * false for this device when it was connecting.
	 * Otherwise the {@link #onLinkLossOccurred(BluetoothDevice)} method will be called instead.
	 *
	 * @param device the device that got disconnected.
	 * @deprecated Use {@link ConnectionObserver#onDeviceDisconnected(BluetoothDevice, int)} or
	 * {@link ConnectionObserver#onDeviceFailedToConnect(BluetoothDevice, int)} instead.
	 */
	@Deprecated
	void onDeviceDisconnected(@NonNull final BluetoothDevice device);

	/**
	 * This callback is invoked when the Ble Manager lost connection to a device that has been
	 * connected with autoConnect option (see {@link ConnectRequest#shouldAutoConnect()}.
	 * Otherwise a {@link #onDeviceDisconnected(BluetoothDevice)} method will be called on such
	 * event.
	 *
	 * @param device the device that got disconnected due to a link loss.
	 * @deprecated Use {@link ConnectionObserver#onDeviceDisconnected(BluetoothDevice, int)} and
	 * await {@link ConnectionObserver#REASON_LINK_LOSS} instead.
	 */
	@Deprecated
	void onLinkLossOccurred(@NonNull final BluetoothDevice device);

	/**
	 * Called when service discovery has finished and primary services has been found.
	 * This method is not called if the primary, mandatory services were not found during service
	 * discovery. For example in the Blood Pressure Monitor, a Blood Pressure service is a
	 * primary service and Intermediate Cuff Pressure service is a optional secondary service.
	 * Existence of battery service is not notified by this call.
	 * <p>
	 * After successful service discovery the service will initialize all services.
	 * The {@link #onDeviceReady(BluetoothDevice)} method will be called when the initialization
	 * is complete.
	 *
	 * @param device                the device which services got disconnected.
	 * @param optionalServicesFound if <code>true</code> the secondary services were also found
	 *                              on the device.
	 * @deprecated This is information internal to the manager. Should it be exposed, it has to be
	 * implemented in the app layer.
	 */
	@Deprecated
	void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound);

	/**
	 * Method called when all initialization requests has been completed.
	 *
	 * @param device the device that get ready.
	 * @deprecated Use {@link ConnectionObserver#onDeviceReady(BluetoothDevice)} instead.
	 */
	@Deprecated
	void onDeviceReady(@NonNull final BluetoothDevice device);

	/**
	 * This method should return true if Battery Level notifications should be enabled on the
	 * target device. If there is no Battery Service, or the Battery Level characteristic does
	 * not have NOTIFY property, this method will not be called for this device.
	 * <p>
	 * This method may return true only if an activity is bound to the service (to display the
	 * information to the user), always (e.g. if critical battery level is reported using
	 * notifications) or never, if such information is not important or the manager wants to
	 * control Battery Level notifications on its own.
	 *
	 * @param device the target device.
	 * @return True to enabled battery level notifications after connecting to the device,
	 * false otherwise.
	 * @deprecated Use
	 * <pre>{@code
	 * setNotificationCallback(batteryLevelCharacteristic)
	 *       .with(new BatteryLevelDataCallback() {
	 *           onBatteryLevelChanged(int batteryLevel) {
	 *                ...
	 *           }
	 *       });
	 * }</pre>
	 * in the {@link BleManager.BleManagerGattCallback#initialize() initialize(BluetoothDevice)}
	 * instead.
	 */
	@Deprecated
	default boolean shouldEnableBatteryLevelNotifications(@NonNull final BluetoothDevice device) {
		return false;
	}

	/**
	 * Called when battery value has been received from the device.
	 *
	 * @param device the device from which the battery value has changed.
	 * @param value  the battery value in percent.
	 * @deprecated Use
	 * <pre>{@code
	 * setNotificationCallback(batteryLevelCharacteristic)
	 *       .with(new BatteryLevelDataCallback() {
	 *           onBatteryLevelChanged(int batteryLevel) {
	 *                ...
	 *           }
	 *       });
	 * }</pre>
	 * in the {@link BleManager.BleManagerGattCallback#initialize() initialize(BluetoothDevice)}
	 * instead.
	 */
	@Deprecated
	default void onBatteryValueReceived(@NonNull final BluetoothDevice device,
										@IntRange(from = 0, to = 100) final int value) {
		// do nothing
	}

	/**
	 * Called when an {@link BluetoothGatt#GATT_INSUFFICIENT_AUTHENTICATION} error occurred and the
	 * device bond state is {@link BluetoothDevice#BOND_NONE}.
	 *
	 * @param device the device that requires bonding.
	 * @deprecated Use {@link BleManager#setBondingObserver(BondingObserver)} instead.
	 */
	@Deprecated
	void onBondingRequired(@NonNull final BluetoothDevice device);

	/**
	 * Called when the device has been successfully bonded.
	 *
	 * @param device the device that got bonded.
	 * @deprecated Use {@link BleManager#setBondingObserver(BondingObserver)} instead.
	 */
	@Deprecated
	void onBonded(@NonNull final BluetoothDevice device);

	/**
	 * Called when the bond state has changed from {@link BluetoothDevice#BOND_BONDING} to
	 * {@link BluetoothDevice#BOND_NONE}.
	 *
	 * @param device the device that failed to bond.
	 * @deprecated Use {@link BleManager#setBondingObserver(BondingObserver)} instead.
	 */
	@Deprecated
	void onBondingFailed(@NonNull final BluetoothDevice device);

	/**
	 * Called when a BLE error has occurred
	 *
	 * @param message   the error message.
	 * @param errorCode the error code.
	 * @param device    the device that caused an error.
	 * @deprecated Use per-request {@link Request#fail(FailCallback)} callback instead.
	 */
	@Deprecated
	void onError(@NonNull final BluetoothDevice device,
				 @NonNull final String message, final int errorCode);

	/**
	 * Called when service discovery has finished but the main services were not found on the device.
	 *
	 * @param device the device that failed to connect due to lack of required services.
	 * @deprecated {@link ConnectRequest#fail(FailCallback)} with reason
	 * {@link FailCallback#REASON_DEVICE_NOT_SUPPORTED} will be called instead.
	 * {@link ConnectionObserver#onDeviceDisconnected(BluetoothDevice, int)} with
	 * {@link ConnectionObserver#REASON_NOT_SUPPORTED} can also be used.
	 */
	@Deprecated
	void onDeviceNotSupported(@NonNull final BluetoothDevice device);
}
