@file:Suppress("unused")

package no.nordicsemi.android.ble.ktx

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.*
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.exception.*
import no.nordicsemi.android.ble.response.ReadResponse
import no.nordicsemi.android.ble.response.WriteResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspends the coroutine until the request is completed.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun Request.suspend() = suspendNonCancellable()

/**
 * Suspends the coroutine until the request is completed.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun TimeoutableRequest.suspend() = suspendCancellable()

/**
 * Suspends the coroutine until the data have been written.
 * @return The data written.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun WriteRequest.suspend(): Data {
	var result: Data? = null
	this
		.with { _, data -> result = data }
		.suspendNonCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the data have been written.
 * The data sent is parsed to the given type.
 *
 * Usage:
 *
 *     // Assuming AlertLevelRequest is a class similar to AlertDataResponse from ble-common
 *     // module, but extends WriteResponse interface instead of ProfileReadResponse.
 *     val result: AlertLevelRequest = writeCharacteristic(
 *             alertLevelCharacteristic,
 *             AlertLevelData.highAlert(),
 *             BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
 *         ).suspendForResponse()
 *
 * @return The data written parsed to required type.
 * @since 2.4.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: WriteResponse> WriteRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	return this
		.before { d -> device = d }
		.suspend()
		.let {
			T::class.java.getDeclaredConstructor().newInstance().apply { onDataSent(device!!, it) }
		}
}

/**
 * Suspends the coroutine until the data have been read.
 * @return The data read.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun ReadRequest.suspend(): Data {
	var result: Data? = null
	this
		.with { _, data -> result = data }
		.suspendNonCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the data have been read.
 * The data read is parsed to the given type.
 *
 * Usage:
 *
 *     val result: AlertLevelResponse = readCharacteristic(alertLevelCharacteristic)
 *         .suspendForResponse()
 *
 * @return The data read parsed to required type.
 * @since 2.4.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: ReadResponse> ReadRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	return this
		.before { d -> device = d }
		.suspend()
		.let {
			T::class.java.getDeclaredConstructor().newInstance().apply { onDataReceived(device!!, it) }
		}
}

/**
 * Suspends the coroutine until the data have been read.
 * The data read is parsed to the given type.
 * If the received data are not valid, an [InvalidDataException] is thrown.
 *
 * Usage:
 *
 *     val result: AlertLevelResponse = readCharacteristic(alertLevelCharacteristic)
 *         .suspendForValidResponse()
 *
 * @return The data read parsed to required type.
 * @since 2.4.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class,
	InvalidDataException::class
)
suspend inline fun <reified T: ProfileReadResponse> ReadRequest.suspendForValidResponse(): T {
	val response = suspendForResponse<T>()
	return response.takeIf { it.isValid } ?: throw InvalidDataException(response)
}

/**
 * Suspends the coroutine until the RSSI value is received.
 * @return The current RSSI value.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun ReadRssiRequest.suspend(): Int {
	var result: Int? = null
	this
		.with { _, rssi -> result = rssi }
		.suspendNonCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the MTU value is received.
 * @return The current MTU value.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun MtuRequest.suspend(): Int {
	var result: Int? = null
	this
		.with { _, mtu -> result = mtu }
		.suspendNonCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the TX and RX PHY values are received.
 * @return A pair of TX and RX PHYs.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun PhyRequest.suspend(): Pair<Int, Int> {
	var result: Pair<Int, Int>? = null
	this
		.with { _, txPhy, rxPhy -> result = txPhy to rxPhy }
		.suspendNonCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the value of the attribute has changed.
 * @return The new value of the attribute.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun WaitForValueChangedRequest.suspend(): Data  = suspendCancellableCoroutine { continuation ->
	continuation.invokeOnCancellation { cancel() }
	var data: Data? = null
	this
		// DON'T USE .before callback here, it's used to get BluetoothDevice instance above.
		.with { _, d -> data = d }
		.invalid { continuation.resumeWithException(InvalidRequestException(this)) }
		.fail { _, status ->
			val exception = when (status) {
				FailCallback.REASON_CANCELLED -> return@fail
				FailCallback.REASON_BLUETOOTH_DISABLED -> BluetoothDisabledException()
				FailCallback.REASON_DEVICE_DISCONNECTED -> DeviceDisconnectedException()
				else -> RequestFailedException(this, status)
			}
			continuation.resumeWithException(exception)
		}
		.done { continuation.resume(data!!) }
		// .then is called after both .done and .fail
		.enqueue()
}

/**
 * Suspends the coroutine until the value of the attribute has changed.
 *
 * Usage:
 *
 *     val result: HeartRateMeasurementResponse = waitForNotification(hmrCharacteristic)
 *         .suspendForResponse()
 *
 * @return The new value of the attribute.
 * @since 2.4.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: ReadResponse> WaitForValueChangedRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	return this
		.before { d -> device = d }
		.suspend()
		.let {
			T::class.java.getDeclaredConstructor().newInstance().apply { onDataReceived(device!!, it) }
		}
}

/**
 * Suspends the coroutine until the value of the attribute has changed and is valid.
 * If the value is invalid, an [InvalidDataException] is thrown.
 *
 * Usage:
 *
 *     val result: HeartRateMeasurementResponse = waitForNotification(hmrCharacteristic)
 *         .suspendForValidResponse()
 *
 * @return The new value of the attribute.
 * @since 2.4.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class,
	InvalidDataException::class
)
suspend inline fun <reified T: ProfileReadResponse> WaitForValueChangedRequest.suspendForValidResponse(): T {
	val response = suspendForResponse<T>()
	return response.takeIf { it.isValid } ?: throw InvalidDataException(response)
}

/**
 * Suspends the coroutine until the value of the attribute has changed.
 * @return The new value of the attribute.
 * @since 2.3.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun WaitForReadRequest.suspend(): Data  = suspendCancellableCoroutine { continuation ->
	continuation.invokeOnCancellation { cancel() }
	var data: Data?	= null
	this
		// Make sure the callbacks are called without unnecessary delay.
		.setHandler(null)
		// DON'T USE .before callback here, it's used to get BluetoothDevice instance above.
		.with { _, d -> data = d }
		.invalid { continuation.resumeWithException(InvalidRequestException(this)) }
		.fail { _, status ->
			val exception = when (status) {
				FailCallback.REASON_CANCELLED -> return@fail
				FailCallback.REASON_BLUETOOTH_DISABLED -> BluetoothDisabledException()
				FailCallback.REASON_DEVICE_DISCONNECTED -> DeviceDisconnectedException()
				else -> RequestFailedException(this, status)
			}
			continuation.resumeWithException(exception)
		}
		.done { continuation.resume(data!!) }
		// .then is called after both .done and .fail
		.enqueue()
}

/**
 * Suspends the coroutine until the remote device reads the value and the response is sent.
 *
 * Usage:
 *
 *     // Assuming AlertLevelRequest is a class similar to AlertDataResponse from ble-common
 *     // module, but extends WriteResponse interface instead of ProfileReadResponse.
 *     val result: AlertLevelRequest = waitForRead(alertLevelCharacteristic)
 *         .suspendForResponse()
 *
 * @return The new value of the attribute.
 * @since 2.4.0
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: WriteResponse> WaitForReadRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	return this
		.before { d -> device = d }
		.suspend()
		.let {
			T::class.java.getDeclaredConstructor().newInstance().apply { onDataSent(device!!, it) }
		}
}

private suspend fun Request.suspendNonCancellable() = suspendCoroutine { continuation ->
	this
		// Make sure the callbacks are called without unnecessary delay.
		.setHandler(null)
		// DON'T USE .before callback here, it's used to get BluetoothDevice instance above.
		.invalid { continuation.resumeWithException(InvalidRequestException(this)) }
		.fail { _, status ->
			val exception = when (status) {
				FailCallback.REASON_BLUETOOTH_DISABLED -> BluetoothDisabledException()
				FailCallback.REASON_DEVICE_DISCONNECTED -> DeviceDisconnectedException()
				else -> RequestFailedException(this, status)
			}
			continuation.resumeWithException(exception)
		}
		.done { continuation.resume(Unit) }
		// .then is called after both .done and .fail
		.enqueue()
}

private suspend fun TimeoutableRequest.suspendCancellable() = suspendCancellableCoroutine { continuation ->
	continuation.invokeOnCancellation { cancel() }
	this
		// Make sure the callbacks are called without unnecessary delay.
		.setHandler(null)
		// DON'T USE .before callback here, it's used to get BluetoothDevice instance above.
		.invalid { continuation.resumeWithException(InvalidRequestException(this)) }
		.fail { _, status ->
			val exception = when (status) {
				FailCallback.REASON_CANCELLED -> return@fail
				FailCallback.REASON_BLUETOOTH_DISABLED -> BluetoothDisabledException()
				FailCallback.REASON_DEVICE_DISCONNECTED -> DeviceDisconnectedException()
				else -> RequestFailedException(this, status)
			}
			continuation.resumeWithException(exception)
		}
		.done { continuation.resume(Unit) }
		// .then is called after both .done and .fail
		.enqueue()
}
