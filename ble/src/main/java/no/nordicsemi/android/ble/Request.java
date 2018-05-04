package no.nordicsemi.android.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.ConditionVariable;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.exception.RequestFailedException;

/**
 * On Android, when multiple BLE operations needs to be done, it is required to wait for a proper
 * {@link BluetoothGattCallback BluetoothGattCallback} callback before calling
 * another operation. In order to make BLE operations easier the BleManager allows to enqueue a request
 * containing all data necessary for a given operation. Requests are performed one after another until the
 * queue is empty. Use static methods from below to instantiate a request and then enqueue them using
 * {@link BleManager#enqueue(Request)}.
 *
 * @param <T> The sent/received value callback type, or Void if n/a.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Request<T> {
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
		WAIT_FOR_VALUE_CHANGE,
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
	final ConditionVariable syncLock;
	SuccessCallback successCallback;
	FailCallback failCallback;
	T valueCallback;

	Request(final @NonNull Type type) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = null;
		this.syncLock = new ConditionVariable(true);
	}

	Request(final @NonNull Type type, final @Nullable BluetoothGattCharacteristic characteristic) {
		this.type = type;
		this.characteristic = characteristic;
		this.descriptor = null;
		this.syncLock = new ConditionVariable(true);
	}

	Request(final @NonNull Type type, final @Nullable BluetoothGattDescriptor descriptor) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = descriptor;
		this.syncLock = new ConditionVariable(true);
	}

	/**
	 * Creates a new request that will start pairing with the device.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static Request<Void> createBond() {
		return new Request<>(Type.CREATE_BOND);
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
	public static WriteRequest newEnableNotificationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.ENABLE_NOTIFICATIONS, characteristic);
	}

	/**
	 * Creates new Disable Notification request. The request will not be executed if given characteristic
	 * is null, does not have NOTIFY property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have notifications disabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newDisableNotificationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.DISABLE_NOTIFICATIONS, characteristic);
	}

	/**
	 * Creates new Enable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have indications enabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newEnableIndicationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.ENABLE_INDICATIONS, characteristic);
	}

	/**
	 * Creates new Disable Indications request. The request will not be executed if given characteristic
	 * is null, does not have INDICATE property or the CCCD. After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have indications disabled
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	public static WriteRequest newDisableIndicationsRequest(final @Nullable BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.DISABLE_INDICATIONS, characteristic);
	}

	/**
	 * Reads the first found Battery Level characteristic value from the first found Battery Service.
	 * If any of them is not found, or the characteristic does not have the READ property this operation will not execute.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 * @deprecated Use {@link #newReadRequest(BluetoothGattCharacteristic)} with BatteryLevelDataCallback
	 * from Android BLE Common Library instead.
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
	 * @deprecated Use {@link #newEnableNotificationsRequest(BluetoothGattCharacteristic)} with BatteryLevelDataCallback
	 * from Android BLE Common Library instead.
	 */
	@NonNull
	@Deprecated
	public static WriteRequest newEnableBatteryLevelNotificationsRequest() {
		return new WriteRequest(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
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
	public static WriteRequest newDisableBatteryLevelNotificationsRequest() {
		return new WriteRequest(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS); // the first Battery Level char from the first Battery Service is used
	}

	/**
	 * Enables indications on Service Changed characteristic if such exists in the Generic Attribute service.
	 * It is required to enable those notifications on bonded devices on older Android versions to be
	 * informed about attributes changes. Android 7+ (or 6+) handles this automatically and no action is required.
	 *
	 * @return the new request that can be enqueued using {@link BleManager#enqueue(Request)} method.
	 */
	@NonNull
	static WriteRequest newEnableServiceChangedIndicationsRequest() {
		return new WriteRequest(Type.ENABLE_SERVICE_CHANGED_INDICATIONS); // the only Service Changed char is used (if such exists)
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
	 * successfully unless {@link #await(int)} or its variant was used, in which case this callback
	 * will be ignored.
	 *
	 * @param callback the callback
	 * @return the request
	 */
	@NonNull
	public Request<T> done(final @NonNull SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	/**
	 * Use to add a callback that will be called in case of the request has failed.
	 * This callback will be ignored if {@link #await(int)} or its variant was used, in which case
	 * it will be ignored and the error will be returned as an exception.
	 *
	 * @param callback the callback
	 * @return the request
	 */
	@NonNull
	public Request<T> fail(final @NonNull FailCallback callback) {
		this.failCallback = callback;
		return this;
	}

	/**
	 * Sets the value callback. When {@link #await(int)} is used this callback will be returned
	 * by that method.
	 *
	 * @param callback the callback
	 * @return the request
	 */
	@NonNull
	protected Request<T> with(final @NonNull T callback) {
		this.valueCallback = callback;
		return this;
	}

	/**
	 * Synchronously waits until the request is done.
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @throws RequestFailedException thrown when the BLE request finished with status other than
	 *                                {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException  thrown when you try to call this method from the main (UI)
	 *                                thread.
	 */
	@SuppressWarnings("ConstantConditions")
	public void await() throws RequestFailedException {
		try {
			await(null, 0);
		} catch (final InterruptedException e) {
			// never happen
		}
	}

	/**
	 * Synchronously waits until the request is done.
	 * Callbacks set using {@link #done(SuccessCallback)} and {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @return the object set with {@link #with(T)}, or null if this methods wasn't called.
	 * @throws RequestFailedException thrown when the BLE request finished with status other than
	 *                                {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws IllegalStateException  thrown when you try to call this method from the main (UI)
	 *                                thread.
	 */
	@Nullable
	public <E extends T> E await(final Class<E> resultClass) throws RequestFailedException {
		try {
			return await(resultClass, 0);
		} catch (final InterruptedException e) {
			// never happen
			return null;
		}
	}

	/**
	 * Synchronously waits until the request is done, for at most given number of milliseconds.
	 * Callbacks set using {@link #done(SuccessCallback)}, {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @param timeout optional timeout in milliseconds
	 * @throws RequestFailedException thrown when the BLE request finished with status other than
	 *                                {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws InterruptedException   thrown if the timeout occurred before the request has finished.
	 * @throws IllegalStateException  thrown when you try to call this method from the main (UI)
	 *                                thread.
	 */
	@SuppressWarnings("ConstantConditions")
	public void await(final int timeout) throws RequestFailedException, InterruptedException {
		await(null, timeout);
	}

	/**
	 * Synchronously waits until the request is done, for at most given number of milliseconds.
	 * Callbacks set using {@link #done(SuccessCallback)}, {@link #fail(FailCallback)} and
	 * {@link #with(T)} will be ignored.
	 * <p>
	 * This method may not be called from the main (UI) thread.
	 * </p>
	 *
	 * @param resultClass the result class. This class will be instantiate so it has to have a default
	 *                    constructor.
	 * @param timeout     optional timeout in milliseconds
	 * @return the object with a response
	 * @throws RequestFailedException thrown when the BLE request finished with status other than
	 *                                {@link BluetoothGatt#GATT_SUCCESS}.
	 * @throws InterruptedException   thrown if the timeout occurred before the request has finished.
	 * @throws IllegalStateException  thrown when you try to call this method from the main (UI)
	 *                                thread.
	 */
	@SuppressWarnings({"NullableProblems", "ConstantConditions"})
	@Nullable
	public <E extends T> E await(final @NonNull Class<E> resultClass, final int timeout) throws RequestFailedException, InterruptedException {
		assertNotMainThread();

		final SuccessCallback sc = successCallback;
		final FailCallback fc = failCallback;
		final T vc = valueCallback;
		try {
			E response = null;
			if (resultClass != null)
				response = resultClass.newInstance();
			syncLock.close();
			final RequestCallback callback = new RequestCallback();
			with(response).done(callback).fail(callback);

			if (!syncLock.block(timeout)) {
				throw new InterruptedException();
			}
			if (!callback.isSuccess()) {
				throw new RequestFailedException(this, callback.status);
			}
			return response;
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + resultClass.getCanonicalName() + " class. Is the default constructor accessible?");
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Couldn't instantiate " + resultClass.getCanonicalName() + " class. Does it have a default constructor with no arguments?");
		} finally {
			successCallback = sc;
			failCallback = fc;
			valueCallback = vc;
		}
	}

	void notifySuccess(final BluetoothDevice device) {
		if (successCallback != null)
			successCallback.onRequestCompleted(device);
	}

	void notifyFail(final BluetoothDevice device, final int status) {
		if (failCallback != null)
			failCallback.onRequestFailed(device, status);
	}

	/**
	 * Asserts that the synchronous method was not called from the UI thread.
	 *
	 * @throws IllegalStateException when called from a UI thread.
	 */
	protected static void assertNotMainThread() throws IllegalStateException {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new IllegalStateException("Cannot execute synchronous operation from the UI thread.");
		}
	}

	private final class RequestCallback implements SuccessCallback, FailCallback {
		private int status = BluetoothGatt.GATT_SUCCESS;

		@Override
		public void onRequestCompleted(final BluetoothDevice device) {
			syncLock.open();
		}

		@Override
		public void onRequestFailed(final BluetoothDevice device, final int status) {
			this.status = status;
			syncLock.open();
		}

		public boolean isSuccess() {
			return this.status == BluetoothGatt.GATT_SUCCESS;
		}
	}
}
