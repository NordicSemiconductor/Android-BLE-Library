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

package no.nordicsemi.android.ble

import android.bluetooth.*
import android.os.ConditionVariable
import android.os.Looper
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.annotation.ConnectionPriority
import no.nordicsemi.android.ble.annotation.PhyMask
import no.nordicsemi.android.ble.annotation.PhyOption
import no.nordicsemi.android.ble.annotation.WriteType
import no.nordicsemi.android.ble.callback.BeforeCallback
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.callback.InvalidRequestCallback
import no.nordicsemi.android.ble.callback.SuccessCallback

/**
 * On Android, when multiple BLE operations needs to be done, it is required to wait for a proper
 * [BluetoothGattCallback] callback before calling another operation.
 * In order to make BLE operations easier the BleManager allows to enqueue a request containing all
 * data necessary for a given operation. Requests are performed one after another until the queue
 * is empty.
 */
abstract class Request {

    internal val syncLock: ConditionVariable
    internal val type: Type
    internal val characteristic: BluetoothGattCharacteristic?
    internal val descriptor: BluetoothGattDescriptor?
    internal var beforeCallback: BeforeCallback? = null
    internal var successCallback: SuccessCallback? = null
    internal var failCallback: FailCallback? = null
    internal var invalidRequestCallback: InvalidRequestCallback? = null
    internal var internalBeforeCallback: BeforeCallback? = null
    internal var internalSuccessCallback: SuccessCallback? = null
    internal var internalFailCallback: FailCallback? = null
    internal var enqueued: Boolean = false
    internal var finished: Boolean = false
    private var manager: BleManager<*>? = null

    internal constructor(type: Type) {
        this.type = type
        this.characteristic = null
        this.descriptor = null
        this.syncLock = ConditionVariable(true)
    }

    internal constructor(type: Type, characteristic: BluetoothGattCharacteristic?) {
        this.type = type
        this.characteristic = characteristic
        this.descriptor = null
        this.syncLock = ConditionVariable(true)
    }

    internal constructor(type: Type, descriptor: BluetoothGattDescriptor?) {
        this.type = type
        this.characteristic = null
        this.descriptor = descriptor
        this.syncLock = ConditionVariable(true)
    }

    /**
     * Sets the [BleManager] instance.
     *
     * @param manager the manager in which the request will be executed.
     */
    internal open fun setManager(manager: BleManager<*>): Request {
        this.manager = manager
        return this
    }

    /**
     * Use to set a completion callback. The callback will be invoked when the operation has
     * finished successfully unless the request was executed synchronously, in which case this
     * callback will be ignored.
     *
     * @param callback the callback.
     * @return The request.
     */
    open fun done(callback: SuccessCallback): Request {
        this.successCallback = callback
        return this
    }

    /**
     * Use to set a callback that will be called in case the request has failed.
     * If the target device wasn't set before executing this request
     * ([BleManager.connect] was never called), the
     * [.invalid] will be used instead, as the
     * [BluetoothDevice] is not known.
     *
     *
     * This callback will be ignored if request was executed synchronously, in which case
     * the error will be returned as an exception.
     *
     * @param callback the callback.
     * @return The request.
     */
    open fun fail(callback: FailCallback): Request {
        this.failCallback = callback
        return this
    }

    /**
     * Used to set internal callback what will be executed before the request is executed.
     *
     * @param callback the callback.
     */
    internal fun internalBefore(callback: BeforeCallback) {
        this.internalBeforeCallback = callback
    }

    /**
     * Used to set internal success callback. The callback will be notified in case the request
     * has completed.
     *
     * @param callback the callback.
     */
    internal fun internalSuccess(callback: SuccessCallback) {
        this.internalSuccessCallback = callback
    }


    /**
     * Used to set internal fail callback. The callback will be notified in case the request
     * has failed.
     *
     * @param callback the callback.
     */
    internal fun internalFail(callback: FailCallback) {
        this.internalFailCallback = callback
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
    open fun invalid(callback: InvalidRequestCallback): Request {
        this.invalidRequestCallback = callback
        return this
    }

    /**
     * Sets a callback that will be executed before the execution of this operation starts.
     *
     * @param callback the callback.
     * @return The request.
     */
    open fun before(callback: BeforeCallback): Request {
        this.beforeCallback = callback
        return this
    }

    /**
     * Enqueues the request for asynchronous execution.
     */
    open fun enqueue() {
        manager!!.enqueue(this)
    }

    internal open fun notifyStarted(device: BluetoothDevice) {
        if (beforeCallback != null)
            beforeCallback!!.onRequestStarted(device)
        if (internalBeforeCallback != null)
            internalBeforeCallback!!.onRequestStarted(device)
    }

    internal open fun notifySuccess(device: BluetoothDevice) {
        if (!finished) {
            finished = true

            if (successCallback != null)
                successCallback!!.onRequestCompleted(device)
            if (internalSuccessCallback != null)
                internalSuccessCallback!!.onRequestCompleted(device)
        }
    }

    internal open fun notifyFail(device: BluetoothDevice, status: Int) {
        if (!finished) {
            finished = true

            if (failCallback != null)
                failCallback!!.onRequestFailed(device, status)
            if (internalFailCallback != null)
                internalFailCallback!!.onRequestFailed(device, status)
        }
    }

    internal open fun notifyInvalidRequest() {
        if (!finished) {
            finished = true

            if (invalidRequestCallback != null)
                invalidRequestCallback!!.onInvalidRequest()
        }
    }

    internal enum class Type {
        SET,
        CONNECT,
        DISCONNECT,
        CREATE_BOND,
        REMOVE_BOND,
        WRITE,
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
        @Deprecated("")
        READ_BATTERY_LEVEL,
        @Deprecated("")
        ENABLE_BATTERY_LEVEL_NOTIFICATIONS,
        @Deprecated("")
        DISABLE_BATTERY_LEVEL_NOTIFICATIONS,
        ENABLE_SERVICE_CHANGED_INDICATIONS,
        REQUEST_MTU,
        REQUEST_CONNECTION_PRIORITY,
        SET_PREFERRED_PHY,
        READ_PHY,
        READ_RSSI,
        REFRESH_CACHE,
        SLEEP
    }

    internal inner class RequestCallback : SuccessCallback, FailCallback, InvalidRequestCallback {
        var status = BluetoothGatt.GATT_SUCCESS

        val isSuccess: Boolean
            get() = this.status == BluetoothGatt.GATT_SUCCESS

        override fun onRequestCompleted(device: BluetoothDevice) {
            syncLock.open()
        }

        override fun onRequestFailed(device: BluetoothDevice, status: Int) {
            this.status = status
            syncLock.open()
        }

        override fun onInvalidRequest() {
            this.status = REASON_REQUEST_INVALID
            syncLock.open()
        }
    }

    companion object {

        const val REASON_REQUEST_INVALID = -1000000

        /**
         * Creates a new connect request. This allows to set a callback to the connect event,
         * just like any other request.
         *
         * @param device the device to connect to.
         * @return The new connect request.
         */
        internal fun connect(device: BluetoothDevice): ConnectRequest {
            return ConnectRequest(Type.CONNECT, device)
        }

        /**
         * Creates a new disconnect request. This allows to set a callback to a disconnect event,
         * just like any other request.
         *
         * @return The new disconnect request.
         */
        internal fun disconnect(): DisconnectRequest {
            return DisconnectRequest(Type.DISCONNECT)
        }

        /**
         * Creates a new request that will start pairing with the device.
         *
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#createBond()} instead."
        )
        fun createBond(): SimpleRequest {
            return SimpleRequest(Type.CREATE_BOND)
        }

        /**
         * Creates a new request that will remove the bonding information from the Android device.
         * This is done using reflections and may not work on all devices.
         *
         *
         * The device will disconnect after calling this method. The success callback will be called
         * after the device got disconnected, when the [BluetoothDevice.getBondState] changes
         * to [BluetoothDevice.BOND_NONE].
         *
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#removeBond()} instead."
        )
        fun removeBond(): SimpleRequest {
            return SimpleRequest(Type.REMOVE_BOND)
        }

        /**
         * Creates new Read Characteristic request. The request will not be executed if given
         * characteristic is null or does not have READ property.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to be read.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#readCharacteristic(BluetoothGattCharacteristic)} instead."
        )
        fun newReadRequest(
            characteristic: BluetoothGattCharacteristic?
        ): ReadRequest {
            return ReadRequest(Type.READ, characteristic)
        }

        /**
         * Creates new Write Characteristic request. The request will not be executed if given
         * characteristic is null or does not have WRITE property.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to be written.
         * @param value          value to be written. The array is copied into another buffer so it's
         * safe to reuse the array again.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[])} instead."
        )
        fun newWriteRequest(
            characteristic: BluetoothGattCharacteristic?,
            value: ByteArray?
        ): WriteRequest {
            return WriteRequest(
                Type.WRITE, characteristic, value, 0,
                value?.size ?: 0,
                characteristic?.writeType ?: BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }

        /**
         * Creates new Write Characteristic request. The request will not be executed if given
         * characteristic is null or does not have WRITE property.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to be written.
         * @param value          value to be written. The array is copied into another buffer so it's
         * safe to reuse the array again.
         * @param writeType      write type to be used, one of
         * [BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT],
         * [BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE].
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, Data)} instead."
        )
        fun newWriteRequest(
            characteristic: BluetoothGattCharacteristic?,
            value: ByteArray?, @WriteType writeType: Int
        ): WriteRequest {
            return WriteRequest(
                Type.WRITE, characteristic, value, 0,
                value?.size ?: 0, writeType
            )
        }

        /**
         * Creates new Write Characteristic request. The request will not be executed if given
         * characteristic is null or does not have WRITE property.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to be written.
         * @param value          value to be written. The array is copied into another buffer so it's
         * safe to reuse the array again.
         * @param offset         the offset from which value has to be copied.
         * @param length         number of bytes to be copied from the value buffer.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[], int, int)}\n" +
                    "      instead."
        )
        fun newWriteRequest(
            characteristic: BluetoothGattCharacteristic?,
            value: ByteArray?,
            @IntRange(from = 0) offset: Int, @IntRange(from = 0) length: Int
        ): WriteRequest {
            return WriteRequest(
                Type.WRITE, characteristic, value, offset, length,
                characteristic?.writeType ?: BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }

        /**
         * Creates new Write Characteristic request. The request will not be executed if given
         * characteristic is null or does not have WRITE property.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to be written.
         * @param value          value to be written. The array is copied into another buffer so it's
         * safe to reuse the array again.
         * @param offset         the offset from which value has to be copied.
         * @param length         number of bytes to be copied from the value buffer.
         * @param writeType      write type to be used, one of
         * [BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT],
         * [BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE] or
         * [BluetoothGattCharacteristic.WRITE_TYPE_SIGNED].
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#writeCharacteristic(BluetoothGattCharacteristic, byte[], int, int)}\n" +
                    "      instead."
        )
        fun newWriteRequest(
            characteristic: BluetoothGattCharacteristic?,
            value: ByteArray?,
            @IntRange(from = 0) offset: Int, @IntRange(from = 0) length: Int,
            @WriteType writeType: Int
        ): WriteRequest {
            return WriteRequest(Type.WRITE, characteristic, value, offset, length, writeType)
        }

        /**
         * Creates new Read Descriptor request. The request will not be executed if given descriptor
         * is null. After the operation is complete a proper callback will be invoked.
         *
         * @param descriptor descriptor to be read.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#readDescriptor(BluetoothGattDescriptor)} instead."
        )
        fun newReadRequest(descriptor: BluetoothGattDescriptor?): ReadRequest {
            return ReadRequest(Type.READ_DESCRIPTOR, descriptor)
        }

        /**
         * Creates new Write Descriptor request. The request will not be executed if given descriptor
         * is null. After the operation is complete a proper callback will be invoked.
         *
         * @param descriptor descriptor to be written.
         * @param value      value to be written. The array is copied into another buffer so it's safe
         * to reuse the array again.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#writeDescriptor(BluetoothGattDescriptor, byte[])} instead."
        )
        fun newWriteRequest(
            descriptor: BluetoothGattDescriptor?,
            value: ByteArray?
        ): WriteRequest {
            return WriteRequest(
                Type.WRITE_DESCRIPTOR, descriptor, value, 0,
                value?.size ?: 0
            )
        }

        /**
         * Creates new Write Descriptor request. The request will not be executed if given descriptor
         * is null. After the operation is complete a proper callback will be invoked.
         *
         * @param descriptor descriptor to be written.
         * @param value      value to be written. The array is copied into another buffer so it's safe
         * to reuse the array again.
         * @param offset     the offset from which value has to be copied.
         * @param length     number of bytes to be copied from the value buffer.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#writeDescriptor(BluetoothGattDescriptor, byte[], int, int)} instead."
        )
        fun newWriteRequest(
            descriptor: BluetoothGattDescriptor?,
            value: ByteArray?,
            @IntRange(from = 0) offset: Int, @IntRange(from = 0) length: Int
        ): WriteRequest {
            return WriteRequest(Type.WRITE_DESCRIPTOR, descriptor, value, offset, length)
        }

        /**
         * Creates new Reliable Write request. All operations that need to be executed
         * reliably should be enqueued inside the returned request before enqueuing it in the
         * BleManager. The library will automatically verify the data sent
         *
         * @return The new request.
         */
        internal fun newReliableWriteRequest(): ReliableWriteRequest {
            return ReliableWriteRequest()
        }

        /**
         * Creates new Begin Reliable Write request.
         *
         * @return The new request.
         */
        internal fun newBeginReliableWriteRequest(): SimpleRequest {
            return SimpleRequest(Type.BEGIN_RELIABLE_WRITE)
        }

        /**
         * Executes Reliable Write sub-procedure. At lease one Write Request must be performed
         * before the Reliable Write is to be executed, otherwise error
         * [no.nordicsemi.android.ble.error.GattError.GATT_INVALID_OFFSET] will be returned.
         *
         * @return The new request.
         */
        internal fun newExecuteReliableWriteRequest(): SimpleRequest {
            return SimpleRequest(Type.EXECUTE_RELIABLE_WRITE)
        }

        /**
         * Aborts Reliable Write sub-procedure. All write requests performed during Reliable Write will
         * be cancelled. At lease one Write Request must be performed before the Reliable Write
         * is to be executed, otherwise error
         * [no.nordicsemi.android.ble.error.GattError.GATT_INVALID_OFFSET] will be returned.
         *
         * @return The new request.
         */
        internal fun newAbortReliableWriteRequest(): SimpleRequest {
            return SimpleRequest(Type.ABORT_RELIABLE_WRITE)
        }

        /**
         * Creates new Enable Notification request. The request will not be executed if given
         * characteristic is null, does not have NOTIFY property or the CCCD.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to have notifications enabled.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#enableNotifications(BluetoothGattCharacteristic)} instead."
        )
        fun newEnableNotificationsRequest(
            characteristic: BluetoothGattCharacteristic?
        ): WriteRequest {
            return WriteRequest(Type.ENABLE_NOTIFICATIONS, characteristic)
        }

        /**
         * Creates new Disable Notification request. The request will not be executed if given
         * characteristic is null, does not have NOTIFY property or the CCCD.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to have notifications disabled.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#disableNotifications(BluetoothGattCharacteristic)} instead."
        )
        fun newDisableNotificationsRequest(
            characteristic: BluetoothGattCharacteristic?
        ): WriteRequest {
            return WriteRequest(Type.DISABLE_NOTIFICATIONS, characteristic)
        }

        /**
         * Creates new Enable Indications request. The request will not be executed if given
         * characteristic is null, does not have INDICATE property or the CCCD.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to have indications enabled.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#enableIndications(BluetoothGattCharacteristic)} instead."
        )
        fun newEnableIndicationsRequest(
            characteristic: BluetoothGattCharacteristic?
        ): WriteRequest {
            return WriteRequest(Type.ENABLE_INDICATIONS, characteristic)
        }

        /**
         * Creates new Disable Indications request. The request will not be executed if given
         * characteristic is null, does not have INDICATE property or the CCCD.
         * After the operation is complete a proper callback will be invoked.
         *
         * @param characteristic characteristic to have indications disabled.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#disableNotifications(BluetoothGattCharacteristic)} instead."
        )
        fun newDisableIndicationsRequest(
            characteristic: BluetoothGattCharacteristic?
        ): WriteRequest {
            return WriteRequest(Type.DISABLE_INDICATIONS, characteristic)
        }

        /**
         * Creates new Wait For Notification request. The request will not be executed if given
         * characteristic is null, does not have NOTIFY property or the CCCD.
         * After the operation is complete a proper callback will be invoked.
         *
         *
         * If the notification should be triggered by another operation (for example writing an
         * op code), set it with [WaitForValueChangedRequest.trigger].
         *
         * @param characteristic characteristic from which a notification should be received.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#waitForNotification(BluetoothGattCharacteristic)} instead."
        )
        fun newWaitForNotificationRequest(
            characteristic: BluetoothGattCharacteristic?
        ): WaitForValueChangedRequest {
            return WaitForValueChangedRequest(Type.WAIT_FOR_NOTIFICATION, characteristic)
        }

        /**
         * Creates new Wait For Indication request. The request will not be executed if given
         * characteristic is null, does not have INDICATE property or the CCCD.
         * After the operation is complete a proper callback will be invoked.
         *
         *
         * If the indication should be triggered by another operation (for example writing an
         * op code), set it with [WaitForValueChangedRequest.trigger].
         *
         * @param characteristic characteristic from which a notification should be received.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#waitForIndication(BluetoothGattCharacteristic)} instead."
        )
        fun newWaitForIndicationRequest(
            characteristic: BluetoothGattCharacteristic?
        ): WaitForValueChangedRequest {
            return WaitForValueChangedRequest(Type.WAIT_FOR_INDICATION, characteristic)
        }

        /**
         * Creates new Read Battery Level request. The first found Battery Level characteristic value
         * from the first found Battery Service. If any of them is not found, or the characteristic
         * does not have the READ property this operation will not execute.
         *
         * @return The new request.
         */
        @Deprecated(
            "Use {@link #newReadRequest(BluetoothGattCharacteristic)} with\n" +
                    "      BatteryLevelDataCallback from Android BLE Common Library instead."
        )
        fun newReadBatteryLevelRequest(): ReadRequest {
            return ReadRequest(Type.READ_BATTERY_LEVEL)
        }

        /**
         * Creates new Enable Notifications on the first found Battery Level characteristic from the
         * first found Battery Service. If any of them is not found, or the characteristic does not
         * have the NOTIFY property this operation will not execute.
         *
         * @return The new request.
         */
        @Deprecated(
            "Use {@link #newEnableNotificationsRequest(BluetoothGattCharacteristic)} with\n" +
                    "      BatteryLevelDataCallback from Android BLE Common Library instead."
        )
        fun newEnableBatteryLevelNotificationsRequest(): WriteRequest {
            return WriteRequest(Type.ENABLE_BATTERY_LEVEL_NOTIFICATIONS)
        }

        /**
         * Creates new Disable Notifications on the first found Battery Level characteristic from the
         * first found Battery Service. If any of them is not found, or the characteristic does not
         * have the NOTIFY property this operation will not execute.
         *
         * @return The new request.
         */
        @Deprecated("Use {@link #newDisableNotificationsRequest(BluetoothGattCharacteristic)} instead.")
        fun newDisableBatteryLevelNotificationsRequest(): WriteRequest {
            return WriteRequest(Type.DISABLE_BATTERY_LEVEL_NOTIFICATIONS)
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
        internal fun newEnableServiceChangedIndicationsRequest(): WriteRequest {
            return WriteRequest(Type.ENABLE_SERVICE_CHANGED_INDICATIONS)
        }

        /**
         * Requests new MTU (Maximum Transfer Unit). This is only supported on Android Lollipop or newer.
         * On older platforms the request will enqueue, but will fail to execute and
         * [.fail] callback will be called.
         * The target device may reject requested value and set a smaller MTU.
         *
         * @param mtu the new MTU. Acceptable values are &lt;23, 517&gt;.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#requestMtu(int)} instead."
        )
        fun newMtuRequest(@IntRange(from = 23, to = 517) mtu: Int): MtuRequest {
            return MtuRequest(Type.REQUEST_MTU, mtu)
        }

        /**
         * Requests the new connection priority. Acceptable values are:
         *
         *  1. [ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH]
         * - Interval: 11.25 -15 ms (Android 6+) and 7.5 - 10 ms (older), latency: 0,
         * supervision timeout: 20 sec,
         *  1. [ConnectionPriorityRequest.CONNECTION_PRIORITY_BALANCED]
         * - Interval: 30 - 50 ms, latency: 0, supervision timeout: 20 sec,
         *  1. [ConnectionPriorityRequest.CONNECTION_PRIORITY_LOW_POWER]
         * - Interval: 100 - 125 ms, latency: 2, supervision timeout: 20 sec.
         *
         * Requesting connection priority is available on Android Lollipop or newer. On older
         * platforms the request will enqueue, but will fail to execute and [.fail]
         * callback will be called.
         *
         * @param priority one of: [ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH],
         * [ConnectionPriorityRequest.CONNECTION_PRIORITY_BALANCED],
         * [ConnectionPriorityRequest.CONNECTION_PRIORITY_LOW_POWER].
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#requestConnectionPriority(int)} instead."
        )
        fun newConnectionPriorityRequest(
            @ConnectionPriority priority: Int
        ): ConnectionPriorityRequest {
            return ConnectionPriorityRequest(Type.REQUEST_CONNECTION_PRIORITY, priority)
        }

        /**
         * Requests the change of preferred PHY for this connections.
         *
         *
         * PHY LE 2M and PHY LE Coded are supported only on Android Oreo or newer.
         * You may safely request other PHYs on older platforms, but the request will not be executed
         * and you will get PHY LE 1M as TX and RX PHY in the callback.
         *
         * @param txPhy      preferred transmitter PHY. Bitwise OR of any of
         * [PhyRequest.PHY_LE_1M_MASK], [PhyRequest.PHY_LE_2M_MASK],
         * and [PhyRequest.PHY_LE_CODED_MASK].
         * @param rxPhy      preferred receiver PHY. Bitwise OR of any of
         * [PhyRequest.PHY_LE_1M_MASK], [PhyRequest.PHY_LE_2M_MASK],
         * and [PhyRequest.PHY_LE_CODED_MASK].
         * @param phyOptions preferred coding to use when transmitting on the LE Coded PHY. Can be one
         * of [PhyRequest.PHY_OPTION_NO_PREFERRED],
         * [PhyRequest.PHY_OPTION_S2] or [PhyRequest.PHY_OPTION_S8].
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#setPreferredPhy(int, int, int)} instead."
        )
        fun newSetPreferredPhyRequest(
            @PhyMask txPhy: Int,
            @PhyMask rxPhy: Int,
            @PhyOption phyOptions: Int
        ): PhyRequest {
            return PhyRequest(Type.SET_PREFERRED_PHY, txPhy, rxPhy, phyOptions)
        }

        /**
         * Reads the current PHY for this connections.
         *
         *
         * PHY LE 2M and PHY LE Coded are supported only on Android Oreo or newer.
         * You may safely read PHY on older platforms, but the request will not be executed
         * and you will get PHY LE 1M as TX and RX PHY in the callback.
         *
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#readPhy()} instead."
        )
        fun newReadPhyRequest(): PhyRequest {
            return PhyRequest(Type.READ_PHY)
        }

        /**
         * Reads the current RSSI (Received Signal Strength Indication).
         *
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#readRssi()} instead."
        )
        fun newReadRssiRequest(): ReadRssiRequest {
            return ReadRssiRequest(Type.READ_RSSI)
        }

        /**
         * Refreshes the device cache. As the [BluetoothGatt.refresh] method is not in the
         * public API (it's hidden, and on Android P it is on a light gray list) it is called
         * using reflections and may be removed in some future Android release or on some devices.
         *
         *
         * There is no callback indicating when the cache has been cleared. This library assumes
         * some time and waits. After the delay, it will start service discovery and clear the
         * task queue. When the service discovery finishes, the
         * [BleManager.BleManagerGattCallback.isRequiredServiceSupported] and
         * [BleManager.BleManagerGattCallback.isOptionalServiceSupported] will
         * be called and the initialization will be performed as if the device has just connected.
         *
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#refreshDeviceCache()} instead."
        )
        fun newRefreshCacheRequest(): SimpleRequest {
            return SimpleRequest(Type.REFRESH_CACHE)
        }

        /**
         * Creates new Sleep request that will postpone next request for given number of milliseconds.
         *
         * @param delay the delay in milliseconds.
         * @return The new request.
         */
        @Deprecated(
            "Access to this method will change to package-only.\n" +
                    "      Use {@link BleManager#sleep(long)} instead."
        )
        fun newSleepRequest(@IntRange(from = 0) delay: Long): SleepRequest {
            return SleepRequest(Type.SLEEP, delay)
        }

        /**
         * Asserts that the synchronous method was not called from the UI thread.
         *
         * @throws IllegalStateException when called from a UI thread.
         */
        @Throws(IllegalStateException::class)
        internal fun assertNotMainThread() {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                throw IllegalStateException("Cannot execute synchronous operation from the UI thread.")
            }
        }
    }
}
