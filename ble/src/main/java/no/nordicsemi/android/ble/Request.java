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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.ble.annotation.ConnectionPriority;
import no.nordicsemi.android.ble.annotation.PhyMask;
import no.nordicsemi.android.ble.annotation.PhyOption;
import no.nordicsemi.android.ble.annotation.WriteType;
import no.nordicsemi.android.ble.callback.BeforeCallback;
import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.InvalidRequestCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;

/**
 * On Android, when multiple BLE operations needs to be done, it is required to wait for a proper
 * {@link BluetoothGattCallback} callback before calling another operation.
 * In order to make BLE operations easier the BleManager allows to enqueue a request containing all
 * data necessary for a given operation. Requests are performed one after another until the queue
 * is empty.
 */
@SuppressWarnings({"unused", "WeakerAccess", "deprecation", "DeprecatedIsStillUsed"})
public abstract class Request {

	enum Type {
		SET,
		CONNECT,
		DISCONNECT,
		CREATE_BOND,
		REMOVE_BOND,
		WRITE,
		NOTIFY,
		INDICATE,
		READ,
		WRITE_DESCRIPTOR,
		READ_DESCRIPTOR,
		BEGIN_RELIABLE_WRITE,
		EXECUTE_RELIABLE_WRITE,
		ABORT_RELIABLE_WRITE,
		ENABLE_NOTIFICATIONS,
		ENABLE_INDICATIONS,
		DISABLE_NOTIFICATIONS,
		DISABLE_INDICATIONS,
		WAIT_FOR_NOTIFICATION,
		WAIT_FOR_INDICATION,
		WAIT_FOR_READ,
		WAIT_FOR_WRITE,
		WAIT_FOR_CONDITION,
		SET_VALUE,
		SET_DESCRIPTOR_VALUE,
		@Deprecated
		READ_BATTERY_LEVEL,
		@Deprecated
		ENABLE_BATTERY_LEVEL_NOTIFICATIONS,
		@Deprecated
		DISABLE_BATTERY_LEVEL_NOTIFICATIONS,
		ENABLE_SERVICE_CHANGED_INDICATIONS,
		REQUEST_MTU,
		REQUEST_CONNECTION_PRIORITY,
		SET_PREFERRED_PHY,
		READ_PHY,
		READ_RSSI,
		REFRESH_CACHE,
		SLEEP,
	}

	protected RequestHandler requestHandler;
	protected Handler handler;

	final ConditionVariable syncLock;
	final Type type;
	final BluetoothGattCharacteristic characteristic;
	final BluetoothGattDescriptor descriptor;
	BeforeCallback beforeCallback;
	SuccessCallback successCallback;
	FailCallback failCallback;
	InvalidRequestCallback invalidRequestCallback;
	BeforeCallback internalBeforeCallback;
	SuccessCallback internalSuccessCallback;
	FailCallback internalFailCallback;
	boolean enqueued;
	boolean started, finished;

	Request(@NonNull final Type type) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = null;
		this.syncLock = new ConditionVariable(true);
	}

	Request(@NonNull final Type type, @Nullable final BluetoothGattCharacteristic characteristic) {
		this.type = type;
		this.characteristic = characteristic;
		this.descriptor = null;
		this.syncLock = new ConditionVariable(true);
	}

	Request(@NonNull final Type type, @Nullable final BluetoothGattDescriptor descriptor) {
		this.type = type;
		this.characteristic = null;
		this.descriptor = descriptor;
		this.syncLock = new ConditionVariable(true);
	}

	/**
	 * Sets the {@link BleManager} instance.
	 *  @param requestHandler the requestHandler in which the request will be executed.
	 *
	 */
	@NonNull
	Request setRequestHandler(@NonNull final RequestHandler requestHandler) {
		this.requestHandler = requestHandler;
		if (this.handler == null) {
			this.handler = requestHandler.getHandler();
		}
		return this;
	}

	/**
	 * Sets the handler that will be used to invoke callbacks. By default, the handler set in
	 * {@link BleManager} will be used.
	 *
	 * @param handler The handler to invoke callbacks for this request.
	 * @return The request.
	 */
	@NonNull
	public Request setHandler(@NonNull final Handler handler) {
		this.handler = handler;
		return this;
	}

	/**
	 * Creates a new connect request. This allows to set a callback to the connect event,
	 * just like any other request.
	 *
	 * @param device the device to connect to.
	 * @return The new connect request.
	 */
	@NonNull
	static ConnectRequest connect(@NonNull final BluetoothDevice device) {
		return new ConnectRequest(Type.CONNECT, device);
	}

	/**
	 * Creates a new disconnect request. This allows to set a callback to a disconnect event,
	 * just like any other request.
	 *
	 * @return The new disconnect request.
	 */
	@NonNull
	static DisconnectRequest disconnect() {
		return new DisconnectRequest(Type.DISCONNECT);
	}

	/**
	 * Creates a new request that will start pairing with the device.
	 *
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#createBond()} instead.
	 */
	@Deprecated
	@NonNull
	public static SimpleRequest createBond() {
		return new SimpleRequest(Type.CREATE_BOND);
	}

	/**
	 * Creates a new request that will remove the bonding information from the Android device.
	 * This is done using reflections and may not work on all devices.
	 * <p>
	 * The device will disconnect after calling this method. The success callback will be called
	 * after the device got disconnected, when the {@link BluetoothDevice#getBondState()} changes
	 * to {@link BluetoothDevice#BOND_NONE}.
	 *
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#removeBond()} instead.
	 */
	@Deprecated
	@NonNull
	public static SimpleRequest removeBond() {
		return new SimpleRequest(Type.REMOVE_BOND);
	}

	/**
	 * Creates new Read Characteristic request. The request will not be executed if given
	 * characteristic is null or does not have READ property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be read.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#readCharacteristic(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	@NonNull
	public static ReadRequest newReadRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new ReadRequest(Type.READ, characteristic);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given
	 * characteristic is null or does not have WRITE property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written.
	 * @param value          value to be written. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[])} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newWriteRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value) {
		return new WriteRequest(Type.WRITE, characteristic, value, 0,
				value != null ? value.length : 0,
				characteristic != null ?
						characteristic.getWriteType() :
						BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given
	 * characteristic is null or does not have WRITE property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written.
	 * @param value          value to be written. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @param writeType      write type to be used, one of
	 *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT},
	 *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE}.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, Data)} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newWriteRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value, @WriteType final int writeType) {
		return new WriteRequest(Type.WRITE, characteristic, value, 0,
				value != null ? value.length : 0, writeType);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given
	 * characteristic is null or does not have WRITE property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written.
	 * @param value          value to be written. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied.
	 * @param length         number of bytes to be copied from the value buffer.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[], int, int)}
	 * instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newWriteRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new WriteRequest(Type.WRITE, characteristic, value, offset, length,
				characteristic != null ?
						characteristic.getWriteType() :
						BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
	}

	/**
	 * Creates new Write Characteristic request. The request will not be executed if given
	 * characteristic is null or does not have WRITE property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be written.
	 * @param value          value to be written. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied.
	 * @param length         number of bytes to be copied from the value buffer.
	 * @param writeType      write type to be used, one of
	 *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_DEFAULT},
	 *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_NO_RESPONSE} or
	 *                       {@link BluetoothGattCharacteristic#WRITE_TYPE_SIGNED}.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[], int, int)}
	 * instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newWriteRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length,
			@WriteType final int writeType) {
		return new WriteRequest(Type.WRITE, characteristic, value, offset, length, writeType);
	}

	/**
	 * Creates new Read Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be read.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#readDescriptor(BluetoothGattDescriptor)} instead.
	 */
	@Deprecated
	@NonNull
	public static ReadRequest newReadRequest(@Nullable final BluetoothGattDescriptor descriptor) {
		return new ReadRequest(Type.READ_DESCRIPTOR, descriptor);
	}

	/**
	 * Creates new Write Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be written.
	 * @param value      value to be written. The array is copied into another buffer so it's safe
	 *                   to reuse the array again.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#writeDescriptor(BluetoothGattDescriptor, byte[])} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newWriteRequest(@Nullable final BluetoothGattDescriptor descriptor,
											   @Nullable final byte[] value) {
		return new WriteRequest(Type.WRITE_DESCRIPTOR, descriptor, value, 0,
				value != null ? value.length : 0);
	}

	/**
	 * Creates new Write Descriptor request. The request will not be executed if given descriptor
	 * is null. After the operation is complete a proper callback will be invoked.
	 *
	 * @param descriptor descriptor to be written.
	 * @param value      value to be written. The array is copied into another buffer so it's safe
	 *                   to reuse the array again.
	 * @param offset     the offset from which value has to be copied.
	 * @param length     number of bytes to be copied from the value buffer.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#writeDescriptor(BluetoothGattDescriptor, byte[], int, int)} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newWriteRequest(
			@Nullable final BluetoothGattDescriptor descriptor,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new WriteRequest(Type.WRITE_DESCRIPTOR, descriptor, value, offset, length);
	}

	/**
	 * Creates new Reliable Write request. All operations that need to be executed
	 * reliably should be enqueued inside the returned request before enqueuing it in the
	 * BleManager. The library will automatically verify the data sent
	 *
	 * @return The new request.
	 */
	@NonNull
	static ReliableWriteRequest newReliableWriteRequest() {
		return new ReliableWriteRequest();
	}

	/**
	 * Creates new Begin Reliable Write request.
	 *
	 * @return The new request.
	 */
	@NonNull
	static SimpleRequest newBeginReliableWriteRequest() {
		return new SimpleRequest(Type.BEGIN_RELIABLE_WRITE);
	}

	/**
	 * Executes Reliable Write sub-procedure. At lease one Write Request must be performed
	 * before the Reliable Write is to be executed, otherwise error
	 * {@link no.nordicsemi.android.ble.error.GattError#GATT_INVALID_OFFSET} will be returned.
	 *
	 * @return The new request.
	 */
	@NonNull
	static SimpleRequest newExecuteReliableWriteRequest() {
		return new SimpleRequest(Type.EXECUTE_RELIABLE_WRITE);
	}

	/**
	 * Aborts Reliable Write sub-procedure. All write requests performed during Reliable Write will
	 * be cancelled. At lease one Write Request must be performed before the Reliable Write
	 * is to be executed, otherwise error
	 * {@link no.nordicsemi.android.ble.error.GattError#GATT_INVALID_OFFSET} will be returned.
	 *
	 * @return The new request.
	 */
	@NonNull
	static SimpleRequest newAbortReliableWriteRequest() {
		return new SimpleRequest(Type.ABORT_RELIABLE_WRITE);
	}

	/**
	 * Creates new Send Notification request. The request will not be executed if given
	 * characteristic is null or does not have NOTIFY property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be notified.
	 * @param value          value to be sent. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @return The new request.
	 */
	@NonNull
	static WriteRequest newNotificationRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value) {
		return new WriteRequest(Type.NOTIFY, characteristic, value, 0,
				value != null ? value.length : 0);
	}

	/**
	 * Creates new Send Notification request. The request will not be executed if given
	 * characteristic is null or does not have NOTIFY property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be notified.
	 * @param value          value to be sent. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied.
	 * @param length         number of bytes to be copied from the value buffer.
	 * @return The new request.
	 */
	@NonNull
	static WriteRequest newNotificationRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new WriteRequest(Type.NOTIFY, characteristic, value, offset, length);
	}

	/**
	 * Creates new Send Indication request. The request will not be executed if given
	 * characteristic is null or does not have INDICATE property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be indicated.
	 * @param value          value to be sent. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @return The new request.
	 */
	@NonNull
	static WriteRequest newIndicationRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value) {
		return new WriteRequest(Type.INDICATE, characteristic, value, 0,
				value != null ? value.length : 0);
	}

	/**
	 * Creates new Send Indication request. The request will not be executed if given
	 * characteristic is null or does not have INDICATE property.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to be indicated.
	 * @param value          value to be sent. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied.
	 * @param length         number of bytes to be copied from the value buffer.
	 * @return The new request.
	 */
	@NonNull
	static WriteRequest newIndicationRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new WriteRequest(Type.INDICATE, characteristic, value, offset, length);
	}

	/**
	 * Creates new Enable Notification request. The request will not be executed if given
	 * characteristic is null, does not have NOTIFY property or the CCCD.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have notifications enabled.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#enableNotifications(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newEnableNotificationsRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.ENABLE_NOTIFICATIONS, characteristic);
	}

	/**
	 * Creates new Disable Notification request. The request will not be executed if given
	 * characteristic is null, does not have NOTIFY property or the CCCD.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have notifications disabled.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#disableNotifications(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newDisableNotificationsRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.DISABLE_NOTIFICATIONS, characteristic);
	}

	/**
	 * Creates new Enable Indications request. The request will not be executed if given
	 * characteristic is null, does not have INDICATE property or the CCCD.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have indications enabled.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#enableIndications(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newEnableIndicationsRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.ENABLE_INDICATIONS, characteristic);
	}

	/**
	 * Creates new Disable Indications request. The request will not be executed if given
	 * characteristic is null, does not have INDICATE property or the CCCD.
	 * After the operation is complete a proper callback will be invoked.
	 *
	 * @param characteristic characteristic to have indications disabled.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#disableNotifications(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	@NonNull
	public static WriteRequest newDisableIndicationsRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WriteRequest(Type.DISABLE_INDICATIONS, characteristic);
	}

	/**
	 * Creates new Wait For Notification request. The request will not be executed if given
	 * characteristic is null, does not have NOTIFY property or the CCCD.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the notification should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param characteristic characteristic from which a notification should be received.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#waitForNotification(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	@NonNull
	public static WaitForValueChangedRequest newWaitForNotificationRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WaitForValueChangedRequest(Type.WAIT_FOR_NOTIFICATION, characteristic);
	}

	/**
	 * Creates new Wait For Indication request. The request will not be executed if given
	 * characteristic is null, does not have INDICATE property or the CCCD.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the indication should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param characteristic characteristic from which a notification should be received.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#waitForIndication(BluetoothGattCharacteristic)} instead.
	 */
	@Deprecated
	@NonNull
	public static WaitForValueChangedRequest newWaitForIndicationRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WaitForValueChangedRequest(Type.WAIT_FOR_INDICATION, characteristic);
	}

	/**
	 * Creates new Wait For Write request. The request will not be executed if given
	 * characteristic is null, or does not have WRITE property.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the write should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param characteristic characteristic that should be written by the remote device.
	 * @return The new request.
	 */
	@NonNull
	static WaitForValueChangedRequest newWaitForWriteRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WaitForValueChangedRequest(Type.WAIT_FOR_WRITE, characteristic);
	}

	/**
	 * Creates new Wait For Write request. The request will not be executed if given
	 * descriptor is null After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the write should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param descriptor descriptor that should be written by the remote device.
	 * @return The new request.
	 */
	@NonNull
	static WaitForValueChangedRequest newWaitForWriteRequest(
			@Nullable final BluetoothGattDescriptor descriptor) {
		return new WaitForValueChangedRequest(Type.WAIT_FOR_WRITE, descriptor);
	}

	/**
	 * Creates new Wait For Read request. The request will not be executed if given server
	 * characteristic is null, or does not have READ property.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the read should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param characteristic characteristic that should be read by the remote device.
	 * @return The new request.
	 */
	@NonNull
	static WaitForReadRequest newWaitForReadRequest(
			@Nullable final BluetoothGattCharacteristic characteristic) {
		return new WaitForReadRequest(Type.WAIT_FOR_READ, characteristic);
	}

	/**
	 * Creates new Wait For Read request. The request will not be executed if given server
	 * characteristic is null, or does not have READ property.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the read should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param characteristic characteristic that should be read by the remote device.
	 * @param value          value to be sent. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @return The new request.
	 */
	@NonNull
	static WaitForReadRequest newWaitForReadRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value) {
		return new WaitForReadRequest(Type.WAIT_FOR_READ, characteristic, value, 0,
				value != null ? value.length : 0);
	}

	/**
	 * Creates new Wait For Read request. The request will not be executed if given server
	 * characteristic is null, or does not have READ property.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the read should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param characteristic characteristic that should be read by the remote device.
	 * @param value          value to be sent. The array is copied into another buffer so it's
	 *                       safe to reuse the array again.
	 * @param offset         the offset from which value has to be copied.
	 * @param length         number of bytes to be copied from the value buffer.
	 * @return The new request.
	 */
	@NonNull
	static WaitForReadRequest newWaitForReadRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new WaitForReadRequest(Type.WAIT_FOR_READ, characteristic, value, offset, length);
	}

	/**
	 * Creates new Wait For Read request. The request will not be executed if given server
	 * characteristic is null, or does not have READ property.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the read should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param descriptor descriptor that should be read by the remote device.
	 * @return The new request.
	 */
	@NonNull
	static WaitForReadRequest newWaitForReadRequest(
			@Nullable final BluetoothGattDescriptor descriptor) {
		return new WaitForReadRequest(Type.WAIT_FOR_READ, descriptor);
	}

	/**
	 * Creates new Wait For Read request. The request will not be executed if given server
	 * characteristic is null, or does not have READ property.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the read should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param descriptor descriptor that should be read by the remote device.
	 * @param value      value to be sent. The array is copied into another buffer so it's
	 *                   safe to reuse the array again.
	 * @return The new request.
	 */
	@NonNull
	static WaitForReadRequest newWaitForReadRequest(
			@Nullable final BluetoothGattDescriptor descriptor,
			@Nullable final byte[] value) {
		return new WaitForReadRequest(Type.WAIT_FOR_READ, descriptor, value, 0,
				value != null ? value.length : 0);
	}

	/**
	 * Creates new Wait For Read request. The request will not be executed if given server
	 * characteristic is null, or does not have READ property.
	 * After the operation is complete a proper callback will be invoked.
	 * <p>
	 * If the read should be triggered by another operation (for example writing an
	 * op code), set it with {@link WaitForValueChangedRequest#trigger(Operation)}.
	 *
	 * @param descriptor descriptor that should be read by the remote device.
	 * @param value      value to be sent. The array is copied into another buffer so it's
	 *                   safe to reuse the array again.
	 * @param offset     the offset from which value has to be copied.
	 * @param length     number of bytes to be copied from the value buffer.
	 * @return The new request.
	 */
	@NonNull
	static WaitForReadRequest newWaitForReadRequest(
			@Nullable final BluetoothGattDescriptor descriptor,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new WaitForReadRequest(Type.WAIT_FOR_READ, descriptor, value, offset, length);
	}

	/**
	 * Creates new Conditional Wait Request. The request will wait until the condition is fulfilled.
	 *
	 * @param condition the condition to check.
	 * @param parameter an optional parameter.
	 * @return The new request.
	 */
	@NonNull
	static <T> ConditionalWaitRequest<T> newConditionalWaitRequest(@NonNull final ConditionalWaitRequest.Condition<T> condition,
																   @Nullable final T parameter) {
		return new ConditionalWaitRequest<>(Type.WAIT_FOR_CONDITION, condition, parameter);
	}

	/**
	 * Creates new Set Characteristic Value request.
	 *
	 * @param characteristic the target characteristic for value set.
	 * @param value          the new data to be assigned.
	 * @return The new request.
	 */
	@NonNull
	static SetValueRequest newSetValueRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value) {
		return new SetValueRequest(Type.SET_VALUE, characteristic, value, 0,
				value != null ? value.length : 0);
	}

	/**
	 * Creates new Set Characteristic Value request.
	 *
	 * @param characteristic the target characteristic for value set.
	 * @param value          the new data to be assigned.
	 * @param offset         the offset from which value has to be copied.
	 * @param length         number of bytes to be copied from the value buffer.
	 * @return The new request.
	 */
	@NonNull
	static SetValueRequest newSetValueRequest(
			@Nullable final BluetoothGattCharacteristic characteristic,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new SetValueRequest(Type.SET_VALUE, characteristic, value, offset, length);
	}

	/**
	 * Creates new Set Descriptor Value request.
	 *
	 * @param descriptor the target descriptor for value set.
	 * @param value      the new data to be assigned.
	 * @return The new request.
	 */
	@NonNull
	static SetValueRequest newSetValueRequest(
			@Nullable final BluetoothGattDescriptor descriptor,
			@Nullable final byte[] value) {
		return new SetValueRequest(Type.SET_DESCRIPTOR_VALUE, descriptor, value, 0,
				value != null ? value.length : 0);
	}

	/**
	 * Creates new Set Descriptor Value request.
	 *
	 * @param descriptor the target descriptor for value set.
	 * @param value      the new data to be assigned.
	 * @param offset     the offset from which value has to be copied.
	 * @param length     number of bytes to be copied from the value buffer.
	 * @return The new request.
	 */
	@NonNull
	static SetValueRequest newSetValueRequest(
			@Nullable final BluetoothGattDescriptor descriptor,
			@Nullable final byte[] value,
			@IntRange(from = 0) final int offset, @IntRange(from = 0) final int length) {
		return new SetValueRequest(Type.SET_DESCRIPTOR_VALUE, descriptor, value, offset, length);
	}

	/**
	 * Creates new Read Battery Level request. The first found Battery Level characteristic value
	 * from the first found Battery Service. If any of them is not found, or the characteristic
	 * does not have the READ property this operation will not execute.
	 *
	 * @return The new request.
	 * @deprecated Use {@link #newReadRequest(BluetoothGattCharacteristic)} with
	 * BatteryLevelDataCallback from Android BLE Common Library instead.
	 */
	@NonNull
	@Deprecated
	public static ReadRequest newReadBatteryLevelRequest() {
		return new ReadRequest(Type.READ_BATTERY_LEVEL);
	}

	/**
	 * Creates new Enable Notifications on the first found Battery Level characteristic from the
	 * first found Battery Service. If any of them is not found, or the characteristic does not
	 * have the NOTIFY property this operation will not execute.
	 *
	 * @return The new request.
	 * @deprecated Use {@link #newEnableNotificationsRequest(BluetoothGattCharacteristic)} with
	 * BatteryLevelDataCallback from Android BLE Common Library instead.
	 */
	@NonNull
	@Deprecated
	public static WriteRequest newEnableBatteryLevelNotificationsRequest() {
		return new WriteRequest(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS);
	}

	/**
	 * Creates new Disable Notifications on the first found Battery Level characteristic from the
	 * first found Battery Service. If any of them is not found, or the characteristic does not
	 * have the NOTIFY property this operation will not execute.
	 *
	 * @return The new request.
	 * @deprecated Use {@link #newDisableNotificationsRequest(BluetoothGattCharacteristic)} instead.
	 */
	@NonNull
	@Deprecated
	public static WriteRequest newDisableBatteryLevelNotificationsRequest() {
		return new WriteRequest(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS);
	}

	/**
	 * Creates new Enable Indications on Service Changed characteristic. It is a NOOP if such
	 * characteristic does not exist in the Generic Attribute service.
	 * It is required to enable those notifications on bonded devices on older Android versions to
	 * be informed about attributes changes.
	 * Android 7+ (or 6+) handles this automatically and no action is required.
	 *
	 * @return The new request.
	 */
	@NonNull
	static WriteRequest newEnableServiceChangedIndicationsRequest() {
		return new WriteRequest(Type.ENABLE_SERVICE_CHANGED_INDICATIONS);
	}

	/**
	 * Requests new MTU (Maximum Transfer Unit). This is only supported on Android Lollipop or newer.
	 * On older platforms the request will enqueue, but will fail to execute and
	 * {@link #fail(FailCallback)} callback will be called.
	 * The target device may reject requested value and set a smaller MTU.
	 *
	 * @param mtu the new MTU. Acceptable values are &lt;23, 517&gt;.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#requestMtu(int)} instead.
	 */
	@Deprecated
	@NonNull
	public static MtuRequest newMtuRequest(@IntRange(from = 23, to = 517) final int mtu) {
		return new MtuRequest(Type.REQUEST_MTU, mtu);
	}

	/**
	 * Requests the new connection priority. Acceptable values are:
	 * <ol>
	 * <li>{@link ConnectionPriorityRequest#CONNECTION_PRIORITY_HIGH}
	 * - Interval: 11.25 -15 ms (Android 6+) and 7.5 - 10 ms (older), latency: 0,
	 * supervision timeout: 20 sec,</li>
	 * <li>{@link ConnectionPriorityRequest#CONNECTION_PRIORITY_BALANCED}
	 * - Interval: 30 - 50 ms, latency: 0, supervision timeout: 20 sec,</li>
	 * <li>{@link ConnectionPriorityRequest#CONNECTION_PRIORITY_LOW_POWER}
	 * - Interval: 100 - 125 ms, latency: 2, supervision timeout: 20 sec.</li>
	 * </ol>
	 * Requesting connection priority is available on Android Lollipop or newer. On older
	 * platforms the request will enqueue, but will fail to execute and {@link #fail(FailCallback)}
	 * callback will be called.
	 *
	 * @param priority one of: {@link ConnectionPriorityRequest#CONNECTION_PRIORITY_HIGH},
	 *                 {@link ConnectionPriorityRequest#CONNECTION_PRIORITY_BALANCED},
	 *                 {@link ConnectionPriorityRequest#CONNECTION_PRIORITY_LOW_POWER}.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#requestConnectionPriority(int)} instead.
	 */
	@Deprecated
	@NonNull
	public static ConnectionPriorityRequest newConnectionPriorityRequest(
			@ConnectionPriority final int priority) {
		return new ConnectionPriorityRequest(Type.REQUEST_CONNECTION_PRIORITY, priority);
	}

	/**
	 * Requests the change of preferred PHY for this connections.
	 * <p>
	 * PHY LE 2M and PHY LE Coded are supported only on Android Oreo or newer.
	 * You may safely request other PHYs on older platforms, but the request will not be executed
	 * and you will get PHY LE 1M as TX and RX PHY in the callback.
	 *
	 * @param txPhy      preferred transmitter PHY. Bitwise OR of any of
	 *                   {@link PhyRequest#PHY_LE_1M_MASK}, {@link PhyRequest#PHY_LE_2M_MASK},
	 *                   and {@link PhyRequest#PHY_LE_CODED_MASK}.
	 * @param rxPhy      preferred receiver PHY. Bitwise OR of any of
	 *                   {@link PhyRequest#PHY_LE_1M_MASK}, {@link PhyRequest#PHY_LE_2M_MASK},
	 *                   and {@link PhyRequest#PHY_LE_CODED_MASK}.
	 * @param phyOptions preferred coding to use when transmitting on the LE Coded PHY. Can be one
	 *                   of {@link PhyRequest#PHY_OPTION_NO_PREFERRED},
	 *                   {@link PhyRequest#PHY_OPTION_S2} or {@link PhyRequest#PHY_OPTION_S8}.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#setPreferredPhy(int, int, int)} instead.
	 */
	@Deprecated
	@NonNull
	public static PhyRequest newSetPreferredPhyRequest(@PhyMask final int txPhy,
													   @PhyMask final int rxPhy,
													   @PhyOption final int phyOptions) {
		return new PhyRequest(Type.SET_PREFERRED_PHY, txPhy, rxPhy, phyOptions);
	}

	/**
	 * Reads the current PHY for this connections.
	 * <p>
	 * PHY LE 2M and PHY LE Coded are supported only on Android Oreo or newer.
	 * You may safely read PHY on older platforms, but the request will not be executed
	 * and you will get PHY LE 1M as TX and RX PHY in the callback.
	 *
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#readPhy()} instead.
	 */
	@Deprecated
	@NonNull
	public static PhyRequest newReadPhyRequest() {
		return new PhyRequest(Type.READ_PHY);
	}

	/**
	 * Reads the current RSSI (Received Signal Strength Indication).
	 *
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#readRssi()} instead.
	 */
	@Deprecated
	@NonNull
	public static ReadRssiRequest newReadRssiRequest() {
		return new ReadRssiRequest(Type.READ_RSSI);
	}

	/**
	 * Refreshes the device cache. As the {@link BluetoothGatt#refresh()} method is not in the
	 * public API (it's hidden, and on Android P it is on a light gray list) it is called
	 * using reflections and may be removed in some future Android release or on some devices.
	 * <p>
	 * There is no callback indicating when the cache has been cleared. This library assumes
	 * some time and waits. After the delay, it will start service discovery and clear the
	 * task queue. When the service discovery finishes, the
	 * {@link BleManager.BleManagerGattCallback#isRequiredServiceSupported(BluetoothGatt)} and
	 * {@link BleManager.BleManagerGattCallback#isOptionalServiceSupported(BluetoothGatt)} will
	 * be called and the initialization will be performed as if the device has just connected.
	 *
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#refreshDeviceCache()} instead.
	 */
	@Deprecated
	@SuppressWarnings("JavadocReference")
	@NonNull
	public static SimpleRequest newRefreshCacheRequest() {
		return new SimpleRequest(Type.REFRESH_CACHE);
	}

	/**
	 * Creates new Sleep request that will postpone next request for given number of milliseconds.
	 *
	 * @param delay the delay in milliseconds.
	 * @return The new request.
	 * @deprecated Access to this method will change to package-only.
	 * Use {@link BleManager#sleep(long)} instead.
	 */
	@Deprecated
	@NonNull
	public static SleepRequest newSleepRequest(@IntRange(from = 0) final long delay) {
		return new SleepRequest(Type.SLEEP, delay);
	}

	/**
	 * Use to set a completion callback. The callback will be invoked when the operation has
	 * finished successfully unless the request was executed synchronously, in which case this
	 * callback will be ignored.
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	public Request done(@NonNull final SuccessCallback callback) {
		this.successCallback = callback;
		return this;
	}

	/**
	 * Use to set a callback that will be called in case the request has failed.
	 * If the target device wasn't set before executing this request
	 * ({@link BleManager#connect(BluetoothDevice)} was never called), the
	 * {@link #invalid(InvalidRequestCallback)} will be used instead, as the
	 * {@link BluetoothDevice} is not known.
	 * <p>
	 * This callback will be ignored if request was executed synchronously, in which case
	 * the error will be returned as an exception.
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	public Request fail(@NonNull final FailCallback callback) {
		this.failCallback = callback;
		return this;
	}

	/**
	 * Used to set internal callback what will be executed before the request is executed.
	 *
	 * @param callback the callback.
	 */
	void internalBefore(@NonNull final BeforeCallback callback) {
		this.internalBeforeCallback = callback;
	}

	/**
	 * Used to set internal success callback. The callback will be notified in case the request
	 * has completed.
	 *
	 * @param callback the callback.
	 */
	void internalSuccess(@NonNull final SuccessCallback callback) {
		this.internalSuccessCallback = callback;
	}


	/**
	 * Used to set internal fail callback. The callback will be notified in case the request
	 * has failed.
	 *
	 * @param callback the callback.
	 */
	void internalFail(@NonNull final FailCallback callback) {
		this.internalFailCallback = callback;
	}

	/**
	 * Use to set a callback that will be called in case the request was invalid, for example
	 * called before the device was connected.
	 * This callback will be ignored if request was executed synchronously, in which case
	 * the error will be returned as an exception.
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	public Request invalid(@NonNull final InvalidRequestCallback callback) {
		this.invalidRequestCallback = callback;
		return this;
	}

	/**
	 * Sets a callback that will be executed before the execution of this operation starts.
	 *
	 * @param callback the callback.
	 * @return The request.
	 */
	@NonNull
	public Request before(@NonNull final BeforeCallback callback) {
		this.beforeCallback = callback;
		return this;
	}

	/**
	 * Enqueues the request for asynchronous execution.
	 */
	public void enqueue() {
		requestHandler.enqueue(this);
	}

	void notifyStarted(@NonNull final BluetoothDevice device) {
		if (!started) {
			started = true;

			handler.post(() -> {
				if (beforeCallback != null)
					beforeCallback.onRequestStarted(device);
				if (internalBeforeCallback != null)
					internalBeforeCallback.onRequestStarted(device);
			});
		}
	}

	void notifySuccess(@NonNull final BluetoothDevice device) {
		if (!finished) {
			finished = true;

			handler.post(() -> {
				if (successCallback != null)
					successCallback.onRequestCompleted(device);
				if (internalSuccessCallback != null)
					internalSuccessCallback.onRequestCompleted(device);
			});
		}
	}

	void notifyFail(@NonNull final BluetoothDevice device, final int status) {
		if (!finished) {
			finished = true;

			handler.post(() -> {
				if (failCallback != null)
					failCallback.onRequestFailed(device, status);
				if (internalFailCallback != null)
					internalFailCallback.onRequestFailed(device, status);
			});
		}
	}

	void notifyInvalidRequest() {
		if (!finished) {
			finished = true;

			handler.post(() -> {
				if (invalidRequestCallback != null)
					invalidRequestCallback.onInvalidRequest();
			});
		}
	}

	/**
	 * Asserts that the synchronous method was not called from the UI thread.
	 *
	 * @throws IllegalStateException when called from a UI thread.
	 */
	static void assertNotMainThread() throws IllegalStateException {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new IllegalStateException("Cannot execute synchronous operation from the UI thread.");
		}
	}

	final class RequestCallback implements SuccessCallback, FailCallback, InvalidRequestCallback {
		final static int REASON_REQUEST_INVALID = -1000000;
		int status = BluetoothGatt.GATT_SUCCESS;

		@Override
		public void onRequestCompleted(@NonNull final BluetoothDevice device) {
			syncLock.open();
		}

		@Override
		public void onRequestFailed(@NonNull final BluetoothDevice device, final int status) {
			this.status = status;
			syncLock.open();
		}

		@Override
		public void onInvalidRequest() {
			this.status = REASON_REQUEST_INVALID;
			syncLock.open();
		}

		@SuppressWarnings("BooleanMethodIsAlwaysInverted")
		boolean isSuccess() {
			return this.status == BluetoothGatt.GATT_SUCCESS;
		}
	}
}
