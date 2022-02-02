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

/**
 * Suspends the coroutine until the request is completed.
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun Request.suspend() = suspendCancellable()

/**
 * Suspends the coroutine until the data have been written.
 * @return The data written.
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun WriteRequest.suspend(): Data {
	var result: Data? = null
	with { _, data -> result = data }.suspendCancellable()
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
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: WriteResponse> WriteRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	then { d -> device = d }.suspend().let {
		return T::class.java.newInstance().apply { onDataSent(device!!, it) }
	}
}

/**
 * Suspends the coroutine until the data have been read.
 * @return The data read.
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun ReadRequest.suspend(): Data {
	var result: Data? = null
	with { _, data -> result = data }.suspendCancellable()
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
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: ReadResponse> ReadRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	then { d -> device = d }.suspend().let {
		return T::class.java.newInstance().apply { onDataReceived(device!!, it) }
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
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun ReadRssiRequest.suspend(): Int {
	var result: Int? = null
	with { _, rssi -> result = rssi }.suspendCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the MTU value is received.
 * @return The current MTU value.
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun MtuRequest.suspend(): Int {
	var result: Int? = null
	with { _, mtu -> result = mtu }.suspendCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the TX and RX PHY values are received.
 * @return A pair of TX and RX PHYs.
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun PhyRequest.suspend(): Pair<Int, Int> {
	var result: Pair<Int, Int>? = null
	with { _, txPhy, rxPhy -> result = txPhy to rxPhy }.suspendCancellable()
	return result!!
}

/**
 * Suspends the coroutine until the value of the attribute has changed.
 * @return The new value of the attribute.
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun WaitForValueChangedRequest.suspend(): Data  = suspendCancellableCoroutine { continuation ->	this
	.with { _, data -> continuation.resume(data) }
	.invalid { continuation.resumeWithException(InvalidRequestException(this)) }
	.fail { _, status ->
		val exception = when (status) {
			FailCallback.REASON_BLUETOOTH_DISABLED -> BluetoothDisabledException()
			FailCallback.REASON_DEVICE_DISCONNECTED -> DeviceDisconnectedException()
			else -> RequestFailedException(this, status)
		}
		continuation.resumeWithException(exception)
	}
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
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: ReadResponse> WaitForValueChangedRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	then { d -> device = d }.suspend().let {
		return T::class.java.newInstance().apply { onDataReceived(device!!, it) }
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
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun WaitForReadRequest.suspend(): Data  = suspendCancellableCoroutine { continuation ->	this
	.with { _, data -> continuation.resume(data) }
	.invalid { continuation.resumeWithException(InvalidRequestException(this)) }
	.fail { _, status ->
		val exception = when (status) {
			FailCallback.REASON_BLUETOOTH_DISABLED -> BluetoothDisabledException()
			FailCallback.REASON_DEVICE_DISCONNECTED -> DeviceDisconnectedException()
			else -> RequestFailedException(this, status)
		}
		continuation.resumeWithException(exception)
	}
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
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend inline fun <reified T: WriteResponse> WaitForReadRequest.suspendForResponse(): T {
	var device: BluetoothDevice? = null
	then { d -> device = d }.suspend().let {
		return T::class.java.newInstance().apply { onDataSent(device!!, it) }
	}
}

private suspend fun Request.suspendCancellable(): Unit = suspendCancellableCoroutine { continuation -> this
	.done { continuation.resume(Unit) }
	.invalid { continuation.resumeWithException(InvalidRequestException(this)) }
	.fail { _, status ->
		val exception = when (status) {
			FailCallback.REASON_BLUETOOTH_DISABLED -> BluetoothDisabledException()
			FailCallback.REASON_DEVICE_DISCONNECTED -> DeviceDisconnectedException()
			else -> RequestFailedException(this, status)
		}
		continuation.resumeWithException(exception)
	}
	.enqueue()
}