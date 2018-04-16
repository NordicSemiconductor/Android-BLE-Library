package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;

/**
 * On Android, when multiple BLE operations needs to be done, it is required to wait for a proper
 * {@link BluetoothGattCallback BluetoothGattCallback} callback before calling
 * another operation. In order to make BLE operations easier the BleManager allows to enqueue a request
 * containing all data necessary for a given operation. Requests are performed one after another until the
 * queue is empty. Use static methods from below to instantiate a request and then enqueue them using
 * {@link BleManager#enqueue(Request)}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Request {
	enum Type {
		CREATE_BOND,
		WRITE,
		READ,
		WRITE_DESCRIPTOR,
		READ_DESCRIPTOR,
		ENABLE_NOTIFICATIONS,
		ENABLE_INDICATIONS,
		DISABLE_NOTIFICATIONS,
		DISABLE_INDICATIONS,
		@Deprecated
		READ_BATTERY_LEVEL,
		@Deprecated
		ENABLE_BATTERY_LEVEL_NOTIFICATIONS,
		@Deprecated
		DISABLE_BATTERY_LEVEL_NOTIFICATIONS,
		ENABLE_SERVICE_CHANGED_INDICATIONS,
		REQUEST_MTU,
		REQUEST_CONNECTION_PRIORITY,
	}

	final Type type;
	final BluetoothGattCharacteristic characteristic;
	final BluetoothGattDescriptor descriptor;
	SuccessCallback successCallback;
	FailCallback failCallback;

	Request(final @NonNull Type type) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = null;
	}

	Request(final @NonNull Type type, final @Nullable BluetoothGattCharacteristic characteristic) {
		this.type = type;
		this.characteristic = characteristic;
		this.descriptor = null;
	}

	Request(final @NonNull Type type, final @Nullable BluetoothGattDescriptor descriptor) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = descriptor;
	}

	/**
	 * Creates a new request that will start pairing with the device.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static Request createBond() {
		return new Request(Type.CREATE_BOND);
	}

	/**
	 * Creates new Read Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have READ property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be read
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static ReadRequest newReadRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new ReadRequest(Type.READ, characteristic);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newWriteRequest(final @Nullable BluetoothGattCharacteristic characteristic, final @Nullable byte[] value) {
		return new WriteRequest(Type.WRITE, characteristic, value, 0, value != null ? value.length : 0, characteristic != null ? characteristic.getWriteType() : BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param writeType      write type to be used, one of {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newWriteRequest(final @Nullable BluetoothGattCharacteristic characteristic, final @Nullable byte[] value, final int writeType) {
		return new WriteRequest(Type.WRITE, characteristic, value, 0, value != null ? value.length : 0, writeType);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied
	 * @param length         number of bytes to be copied from the value buffer
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newWriteRequest(final @Nullable BluetoothGattCharacteristic characteristic, final @Nullable byte[] value, final int offset, final int length) {
		return new WriteRequest(Type.WRITE, characteristic, value, offset, length, characteristic != null ? characteristic.getWriteType() : BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied
	 * @param length         number of bytes to be copied from the value buffer
	 * @param writeType      write type to be used, one of {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newWriteRequest(final @Nullable BluetoothGattCharacteristic characteristic, @Nullable final byte[] value, final int offset, final int length, final int writeType) {
		return new WriteRequest(Type.WRITE, characteristic, value, offset, length, writeType);
	}

	/**
	 * Creates new Read Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be read
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static ReadRequest newReadRequest(final @Nullable BluetoothGattDescriptor descriptor) {
		return new ReadRequest(Type.READ_DESCRIPTOR, descriptor);
	}

	/**
	 * Creates new Write Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be written
	 * @param value      value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newWriteRequest(final @Nullable BluetoothGattDescriptor descriptor, final @Nullable byte[] value) {
		return new WriteRequest(Type.WRITE_DESCRIPTOR, descriptor, value, 0, value != null ? value.length : 0);
	}

	/**
	 * Creates new Write Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be written
	 * @param value      value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param offset     the offset from which value has to be copied
	 * @param length     number of bytes to be copied from the value buffer
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newWriteRequest(final @Nullable BluetoothGattDescriptor descriptor, final byte[] value, final int offset, final int length) {
		return new WriteRequest(Type.WRITE_DESCRIPTOR, descriptor, value, offset, length);
	}

	/**
	 * Creates new Enable Notification request. The request will not be executed if given characteristic
	 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have notifications enabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static ReadRequest newEnableNotificationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new ReadRequest(Type.ENABLE_NOTIFICATIONS, characteristic);
	}

	/**
	 * Creates new Disable Notification request. The request will not be executed if given characteristic
	 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
	 * @param characteristic characteristic to have notifications disabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static Request newDisableNotificationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new Request(Type.DISABLE_NOTIFICATIONS, characteristic);
	}

	/**
	 * Creates new Enable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have indications enabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static ReadRequest newEnableIndicationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new ReadRequest(Type.ENABLE_INDICATIONS, characteristic);
	}

	/**
	 * Creates new Disable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 * @param characteristic characteristic to have indications disabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static Request newDisableIndicationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new Request(Type.DISABLE_INDICATIONS, characteristic);
     }

	/**
	 * Reads the first found Battery Level characteristic value from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the READ property this operation will not execute.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 * @deprecated Use {@link #newReadRequest(BluetoothGattCharacteristic)} with {@link no.nordicsemi.android.ble.callback.profile.BatteryLevelCallback BatteryLevelCallback} instead.
	 */
	@NonNull
	@Deprecated
	public static ReadRequest newReadBatteryLevelRequest() {
		return new ReadRequest(Type.READ_BATTERY_LEVEL); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Enables notifications on the first found Battery Level characteristic from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 * @deprecated Use {@link #newEnableNotificationsRequest(BluetoothGattCharacteristic)} with {@link no.nordicsemi.android.ble.callback.profile.BatteryLevelCallback BatteryLevelCallback} instead.
	 */
	@NonNull
	@Deprecated
	public static ReadRequest newEnableBatteryLevelNotificationsRequest() {
		return new ReadRequest(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Disables notifications on the first found Battery Level characteristic from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 * @deprecated Use {@link #newDisableNotificationsRequest(BluetoothGattCharacteristic)} instead.
	 */
	@NonNull
	@Deprecated
	public static Request newDisableBatteryLevelNotificationsRequest() {
		return new Request(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Enables indications on Service Changed characteristic if such exists in the Generic Attribute service.
	 * It is required to enable those notifications on bonded devices on older Android versions to be
	 * informed about attributes changes. Android 7+ (or 6+) handles this automatically and no action is required.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	static Request newEnableServiceChangedIndicationsRequest() {
		return new Request(Type.ENABLE_SERVICE_CHANGED_INDICATIONS); // the only Service Changed char is used (if such exists)
	}

	/**
	 * Requests new MTU (Maximum Transfer Unit). This is only supported on Android Lollipop or newer.
	 * The target device may reject requested value and set smalled MTU.
	 *
	 * @param mtu the new MTU. Acceptable values are &lt;23, 517&gt;.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static MtuRequest newMtuRequest(final int mtu) {
		return new MtuRequest(Type.REQUEST_MTU, mtu);
	}

	/**
	 * Requests the new connection priority. Acceptable values are:
	 * <ol>
	 * <li>{@link BluetoothGatt#CONNECTION_PRIORITY_HIGH} - Interval: 11.25 -15 ms, latency: 0, supervision timeout: 20 sec,</li>
	 * <li>{@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED} - Interval: 30 - 50 ms, latency: 0, supervision timeout: 20 sec,</li>
	 * <li>{@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER} - Interval: 100 - 125 ms, latency: 2, supervision timeout: 20 sec.</li>
	 * </ol>
	 *
	 * @param priority one of: {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}, {@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED},
	 *                 {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static ConnectionPriorityRequest newConnectionPriorityRequest(final int priority) {
		return new ConnectionPriorityRequest(Type.REQUEST_CONNECTION_PRIORITY, priority);
	}

	/**
	 * Use to add a completion callback. The callback will be invoked when the operation has finished
	 * successfully.
	 * @param callback the callback
	 * @return the request
	 */
	@NonNull
	public Request done(final @NonNull SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	/**
	 * Use to add a callback that will be called in case of the request has failed.
	 * @param callback the callback
	 * @return the request
	 */
	@NonNull
	public Request fail(final @NonNull FailCallback callback) {
		this.failCallback = callback;
		return this;
	}

	void notifySuccess() {
		if (successCallback != null)
			successCallback.onRequestCompleted();
	}

	void notifyFail(final int status) {
		if (failCallback != null)
			failCallback.onRequestFailed(status);
	}
}
