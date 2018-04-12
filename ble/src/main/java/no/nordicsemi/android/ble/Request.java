package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * On Android, when multiple BLE operations needs to be done, it is required to wait for a proper
 * {@link BluetoothGattCallback BluetoothGattCallback} callback before calling
 * another operation. In order to make BLE operations easier the BleManager allows to enqueue a request
 * containing all data necessary for a given operation. Requests are performed one after another until the
 * queue is empty. Use static methods from below to instantiate a request and then enqueue them using
 * {@link BleManager#enqueue(Request)}.
 */
public final class Request {
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
		READ_BATTERY_LEVEL,
		ENABLE_BATTERY_LEVEL_NOTIFICATIONS,
		DISABLE_BATTERY_LEVEL_NOTIFICATIONS,
		ENABLE_SERVICE_CHANGED_INDICATIONS,
		REQUEST_MTU,
		REQUEST_CONNECTION_PRIORITY,
	}

	final Type type;
	final BluetoothGattCharacteristic characteristic;
	final BluetoothGattDescriptor descriptor;
	final RequestCompletionCallback callback;
	final byte[] data;
	final int writeType;
	final int value;

	private Request(final Type type, final RequestCompletionCallback callback) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = null;
		this.data = null;
		this.writeType = 0;
		this.value = 0;
		this.callback = callback;
	}

	private Request(final Type type, final int value, final RequestCompletionCallback callback) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = null;
		this.data = null;
		this.writeType = 0;
		this.value = value;
		this.callback = callback;
	}

	private Request(final Type type, final BluetoothGattCharacteristic characteristic, final RequestCompletionCallback callback) {
		this.type = type;
		this.characteristic = characteristic;
		this.descriptor = null;
		this.data = null;
		this.writeType = 0;
		this.value = 0;
		this.callback = callback;
	}

	private Request(final Type type, final BluetoothGattCharacteristic characteristic, final int writeType, final byte[] data, final int offset, final int length, final RequestCompletionCallback callback) {
		this.type = type;
		this.characteristic = characteristic;
		this.descriptor = null;
		this.data = copy(data, offset, length);
		this.writeType = writeType;
		this.value = 0;
		this.callback = callback;
	}

	private Request(final Type type, final BluetoothGattDescriptor descriptor, final RequestCompletionCallback callback) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = descriptor;
		this.data = null;
		this.writeType = 0;
		this.value = 0;
		this.callback = callback;
	}

	private Request(final Type type, final BluetoothGattDescriptor descriptor, final byte[] data, final int offset, final int length, final RequestCompletionCallback callback) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = descriptor;
		this.data = copy(data, offset, length);
		this.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
		this.value = 0;
		this.callback = callback;
	}

	private static byte[] copy(final byte[] value, final int offset, final int length) {
		if (value == null || offset > value.length)
			return null;
		final int maxLength = Math.min(value.length - offset, length);
		final byte[] copy = new byte[maxLength];
		System.arraycopy(value, offset, copy, 0, maxLength);
		return copy;
	}

	/**
	 * Creates a new request that will start pairing with the device.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request createBond() {
		return new Request(Type.CREATE_BOND, null);
	}

	/**
	 * Creates a new request that will start pairing with the device.
	 * @param callback optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request createBond(final RequestCompletionCallback callback) {
		return new Request(Type.CREATE_BOND, callback);
	}

	/**
	 * Creates new Read Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have READ property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be read
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newReadRequest(final BluetoothGattCharacteristic characteristic) {
		return new Request(Type.READ, characteristic, null);
	}

	/**
	 * Creates new Read Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have READ property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be read
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newReadRequest(final BluetoothGattCharacteristic characteristic, final RequestCompletionCallback callback) {
		return new Request(Type.READ, characteristic, callback);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value) {
		return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), value, 0, value != null ? value.length : 0, null);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final RequestCompletionCallback callback) {
		return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), value, 0, value != null ? value.length : 0, callback);
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
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int writeType) {
		return new Request(Type.WRITE, characteristic, writeType, value, 0, value != null ? value.length : 0, null);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param writeType      write type to be used, one of {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT}, {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int writeType, final RequestCompletionCallback callback) {
		return new Request(Type.WRITE, characteristic, writeType, value, 0, value != null ? value.length : 0, callback);
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
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int offset, final int length) {
		return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), value, offset, length, null);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given characteristic
	 * is null or does not have WRITE property. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written
	 * @param value          value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied
	 * @param length         number of bytes to be copied from the value buffer
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int offset, final int length, final RequestCompletionCallback callback) {
		return new Request(Type.WRITE, characteristic, characteristic.getWriteType(), value, offset, length, callback);
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
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int offset, final int length, final int writeType) {
		return new Request(Type.WRITE, characteristic, writeType, value, offset, length, null);
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
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattCharacteristic characteristic, final byte[] value, final int offset, final int length, final int writeType, final RequestCompletionCallback callback) {
		return new Request(Type.WRITE, characteristic, writeType, value, offset, length, callback);
	}

	/**
	 * Creates new Read Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be read
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newReadRequest(final BluetoothGattDescriptor descriptor) {
		return new Request(Type.READ_DESCRIPTOR, descriptor, null);
	}

	/**
	 * Creates new Read Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be read
	 * @param callback   optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newReadRequest(final BluetoothGattDescriptor descriptor, final RequestCompletionCallback callback) {
		return new Request(Type.READ_DESCRIPTOR, descriptor, callback);
	}

	/**
	 * Creates new Write Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be written
	 * @param value      value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] value) {
		return new Request(Type.WRITE_DESCRIPTOR, descriptor, value, 0, value != null ? value.length : 0, null);
	}

	/**
	 * Creates new Write Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be written
	 * @param value      value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param callback   optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] value, final RequestCompletionCallback callback) {
		return new Request(Type.WRITE_DESCRIPTOR, descriptor, value, 0, value != null ? value.length : 0, callback);
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
	public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] value, final int offset, final int length) {
		return new Request(Type.WRITE_DESCRIPTOR, descriptor, value, offset, length, null);
	}

	/**
	 * Creates new Write Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be written
	 * @param value      value to be written. The array is copied into another buffer so it's safe to reuse the array again.
	 * @param offset     the offset from which value has to be copied
	 * @param length     number of bytes to be copied from the value buffer
	 * @param callback   optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newWriteRequest(final BluetoothGattDescriptor descriptor, final byte[] value, final int offset, final int length, final RequestCompletionCallback callback) {
		return new Request(Type.WRITE_DESCRIPTOR, descriptor, value, offset, length, callback);
	}

	/**
	 * Creates new Enable Notification request. The request will not be executed if given characteristic
	 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have notifications enabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newEnableNotificationsRequest(final BluetoothGattCharacteristic characteristic) {
		return new Request(Type.ENABLE_NOTIFICATIONS, characteristic, null);
	}

	/**
	 * Creates new Enable Notification request. The request will not be executed if given characteristic
	 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have notifications enabled
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newEnableNotificationsRequest(final BluetoothGattCharacteristic characteristic, final RequestCompletionCallback callback) {
		return new Request(Type.ENABLE_NOTIFICATIONS, characteristic, callback);
	}

	/**
	 * Creates new Disable Notification request. The request will not be executed if given characteristic
	 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
	 * @param characteristic characteristic to have notifications disabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newDisableNotificationsRequest(final BluetoothGattCharacteristic characteristic) {
		return new Request(Type.DISABLE_NOTIFICATIONS, characteristic, null);
	}

	/**
	 * Creates new Disable Notification request. The request will not be executed if given characteristic
	 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
	 * @param characteristic characteristic to have notifications disabled
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newDisableNotificationsRequest(final BluetoothGattCharacteristic characteristic, final RequestCompletionCallback callback) {
		return new Request(Type.DISABLE_NOTIFICATIONS, characteristic, callback);
	}

	/**
	 * Creates new Enable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have indications enabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newEnableIndicationsRequest(final BluetoothGattCharacteristic characteristic) {
		return new Request(Type.ENABLE_INDICATIONS, characteristic, null);
	}

	/**
	 * Creates new Enable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have indications enabled
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newEnableIndicationsRequest(final BluetoothGattCharacteristic characteristic, final RequestCompletionCallback callback) {
		return new Request(Type.ENABLE_INDICATIONS, characteristic, callback);
	}

	/**
	 * Creates new Disable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 * @param characteristic characteristic to have indications disabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newDisableIndicationsRequest(final BluetoothGattCharacteristic characteristic) {
		return new Request(Type.DISABLE_INDICATIONS, characteristic, null);
     }

	/**
	 * Creates new Disable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 * @param characteristic characteristic to have indications disabled
	 * @param callback       optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newDisableIndicationsRequest(final BluetoothGattCharacteristic characteristic, final RequestCompletionCallback callback) {
		return new Request(Type.DISABLE_INDICATIONS, characteristic, callback);
	}

	/**
	 * Reads the first found Battery Level characteristic value from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the READ property this operation will not execute.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newReadBatteryLevelRequest() {
		return new Request(Type.READ_BATTERY_LEVEL, null); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Reads the first found Battery Level characteristic value from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the READ property this operation will not execute.
	 * @param callback optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newReadBatteryLevelRequest(final RequestCompletionCallback callback) {
		return new Request(Type.READ_BATTERY_LEVEL, callback); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Enables notifications on the first found Battery Level characteristic from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newEnableBatteryLevelNotificationsRequest() {
		return new Request(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS, null); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Enables notifications on the first found Battery Level characteristic from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
	 * @param callback optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newEnableBatteryLevelNotificationsRequest(final RequestCompletionCallback callback) {
		return new Request(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS, callback); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Disables notifications on the first found Battery Level characteristic from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newDisableBatteryLevelNotificationsRequest() {
		return new Request(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS, null); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Disables notifications on the first found Battery Level characteristic from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the NOTIFY property this operation will not execute.
	 * @param callback optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newDisableBatteryLevelNotificationsRequest(final RequestCompletionCallback callback) {
		return new Request(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS, callback); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Enables indications on Service Changed characteristic if such exists in the Generic Attribute service.
	 * It is required to enable those notifications on bonded devices on older Android versions to be
	 * informed about attributes changes. Android 7+ (or 6+) handles this automatically and no action is required.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	static Request newEnableServiceChangedIndicationsRequest() {
		return new Request(Type.ENABLE_SERVICE_CHANGED_INDICATIONS, null); // the only Service Changed char is used (if such exists)
	}

	/**
	 * Requests new MTU (Maximum Transfer Unit). This is only supported on Android Lollipop or newer.
	 * The target device may reject requested value and set smalled MTU.
	 *
	 * @param mtu the new MTU. Acceptable values are &lt;23, 517&gt;.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newMtuRequest(int mtu) {
		if (mtu < 23)
			mtu = 23;
		if (mtu > 517)
			mtu = 517;
		return new Request(Type.REQUEST_MTU, mtu, null);
	}

	/**
	 * Requests new MTU (Maximum Transfer Unit). This is only supported on Android Lollipop or newer.
	 * The target device may reject requested value and set smalled MTU.
	 *
	 * @param mtu the new MTU. Acceptable values are &lt;23, 517&gt;.
	 * @param callback optional callback for the request
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newMtuRequest(int mtu, final RequestCompletionCallback callback) {
		if (mtu < 23)
			mtu = 23;
		if (mtu > 517)
			mtu = 517;
		return new Request(Type.REQUEST_MTU, mtu, callback);
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
	public static Request newConnectionPriorityRequest(int priority) {
		if (priority < 0 || priority > 2)
			priority = 0; // Balanced
		return new Request(Type.REQUEST_CONNECTION_PRIORITY, priority, null);
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
	 * @param callback optional callback for the request. The callback will be invoked after 100ms on Android 4.3 - 7.x.
	 *                 On Android 8.0 it will be called after receiving onConnectionUpdated(...) GATT callback.
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	public static Request newConnectionPriorityRequest(int priority, final RequestCompletionCallback callback) {
		if (priority < 0 || priority > 2)
			priority = 0; // Balanced
		return new Request(Type.REQUEST_CONNECTION_PRIORITY, priority, callback);
	}

	void completed(final boolean success) {
		if (callback != null)
			callback.onRequestCompleted(success);
	}
}
