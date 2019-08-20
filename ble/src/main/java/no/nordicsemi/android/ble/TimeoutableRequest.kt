package no.nordicsemi.android.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Handler
import androidx.annotation.IntRange
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.exception.BluetoothDisabledException
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException
import no.nordicsemi.android.ble.exception.InvalidRequestException
import no.nordicsemi.android.ble.exception.RequestFailedException

abstract class TimeoutableRequest : Request {
    protected var timeout: Long = 0
    private var timeoutHandler: TimeoutHandler? = null
    private var timeoutCallback: Runnable? = null
    private var handler: Handler? = null

    internal constructor(type: Request.Type) : super(type)

    internal constructor(type: Request.Type, characteristic: BluetoothGattCharacteristic?) : super(
        type,
        characteristic
    )

    internal constructor(type: Request.Type, descriptor: BluetoothGattDescriptor?) : super(
        type,
        descriptor
    )

    override fun setManager(manager: BleManager<*>): TimeoutableRequest {
        super.setManager(manager)
        this.handler = manager.mHandler
        this.timeoutHandler = manager
        return this
    }

    /**
     * Sets the operation timeout.
     * When the timeout occurs, the request will fail with [FailCallback.REASON_TIMEOUT].
     *
     * @param timeout the request timeout in milliseconds, 0 to disable timeout.
     * @return the callback.
     * @throws IllegalStateException         thrown when the request has already been started.
     * @throws UnsupportedOperationException thrown when the timeout is not allowed for this request,
     * as the callback from the system is required.
     */
    open fun timeout(@IntRange(from = 0) timeout: Long): TimeoutableRequest {
        if (timeoutCallback != null)
            throw IllegalStateException("Request already started")
        this.timeout = timeout
        return this
    }

    /**
     * Enqueues the request for asynchronous execution.
     *
     *
     * When the timeout occurs, the request will fail with [FailCallback.REASON_TIMEOUT]
     * and the device will get disconnected.
     *
     * @param timeout the request timeout in milliseconds, 0 to disable timeout. This value will
     * override one set in [.timeout].
     */
    @Deprecated("Use {@link #timeout(long)} and {@link #enqueue()} instead.")
    fun enqueue(@IntRange(from = 0) timeout: Long) {
        timeout(timeout).enqueue()
    }

    /**
     * Synchronously waits until the request is done.
     *
     *
     * Use [.timeout] to set the maximum time the manager should wait until the request
     * is ready. When the timeout occurs, the [InterruptedException] will be thrown.
     *
     *
     * Callbacks set using [.done] and [.fail]
     * will be ignored.
     *
     *
     * This method may not be called from the main (UI) thread.
     *
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [BluetoothGatt.GATT_SUCCESS].
     * @throws InterruptedException        thrown if the timeout occurred before the request has
     * finished.
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     * @see .enqueue
     */
    @Throws(
        RequestFailedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class,
        InterruptedException::class
    )
    fun await() {
        Request.assertNotMainThread()

        val sc = successCallback
        val fc = failCallback
        try {
            syncLock.close()
            val callback = RequestCallback()
            done(callback).fail(callback).invalid(callback).enqueue()

            if (!syncLock.block(timeout)) {
                throw InterruptedException()
            }
            if (!callback.isSuccess) {
                if (callback.status == FailCallback.REASON_DEVICE_DISCONNECTED) {
                    throw DeviceDisconnectedException()
                }
                if (callback.status == FailCallback.REASON_BLUETOOTH_DISABLED) {
                    throw BluetoothDisabledException()
                }
                if (callback.status == Request.REASON_REQUEST_INVALID) {
                    throw InvalidRequestException(this)
                }
                throw RequestFailedException(this, callback.status)
            }
        } finally {
            successCallback = sc
            failCallback = fc
        }
    }

    /**
     * Synchronously waits, for as most as the given number of milliseconds, until the request
     * is ready.
     *
     *
     * When the timeout occurs, the [InterruptedException] will be thrown.
     *
     *
     * Callbacks set using [.done] and [.fail]
     * will be ignored.
     *
     *
     * This method may not be called from the main (UI) thread.
     *
     * @param timeout optional timeout in milliseconds, 0 to disable timeout. This will
     * override the timeout set using [.timeout].
     * @throws RequestFailedException      thrown when the BLE request finished with status other
     * than [BluetoothGatt.GATT_SUCCESS].
     * @throws InterruptedException        thrown if the timeout occurred before the request has
     * finished.
     * @throws IllegalStateException       thrown when you try to call this method from the main
     * (UI) thread.
     * @throws DeviceDisconnectedException thrown when the device disconnected before the request
     * was completed.
     * @throws BluetoothDisabledException  thrown when the Bluetooth adapter has been disabled.
     * @throws InvalidRequestException     thrown when the request was called before the device was
     * connected at least once (unknown device).
     */
    @Deprecated("Use {@link #timeout(long)} and {@link #await()} instead.")
    @Throws(
        RequestFailedException::class,
        InterruptedException::class,
        DeviceDisconnectedException::class,
        BluetoothDisabledException::class,
        InvalidRequestException::class
    )
    fun await(@IntRange(from = 0) timeout: Long) {
        timeout(timeout).await()
    }

    override fun notifyStarted(device: BluetoothDevice) {
        if (timeout > 0L) {
            timeoutCallback = Runnable {
                timeoutCallback = null
                if (!finished) {
                    notifyFail(device, FailCallback.REASON_TIMEOUT)
                    timeoutHandler!!.onRequestTimeout(this)
                }
            }
            handler!!.postDelayed(timeoutCallback, timeout)
        }
        super.notifyStarted(device)
    }

    override fun notifySuccess(device: BluetoothDevice) {
        if (!finished) {
            handler!!.removeCallbacks(timeoutCallback)
            timeoutCallback = null
        }
        super.notifySuccess(device)
    }

    override fun notifyFail(device: BluetoothDevice, status: Int) {
        if (!finished) {
            handler!!.removeCallbacks(timeoutCallback)
            timeoutCallback = null
        }
        super.notifyFail(device, status)
    }

    override fun notifyInvalidRequest() {
        if (!finished) {
            handler!!.removeCallbacks(timeoutCallback)
            timeoutCallback = null
        }
        super.notifyInvalidRequest()
    }
}
