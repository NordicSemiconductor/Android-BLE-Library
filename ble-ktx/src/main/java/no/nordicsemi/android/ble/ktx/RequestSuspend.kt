package no.nordicsemi.android.ble.ktx

import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.*
import no.nordicsemi.android.ble.callback.FailCallback
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.exception.BluetoothDisabledException
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException
import no.nordicsemi.android.ble.exception.InvalidRequestException
import no.nordicsemi.android.ble.exception.RequestFailedException
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
suspend fun PhyRequest.suspend():  Pair<Int, Int> {
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
	.with { _, data ->continuation.resume(data) }
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
 * @return The new value of the attribute.
 */
@Throws(
	BluetoothDisabledException::class,
	DeviceDisconnectedException::class,
	RequestFailedException::class,
	InvalidRequestException::class
)
suspend fun WaitForReadRequest.suspend(): Data  = suspendCancellableCoroutine { continuation ->	this
	.with { _, data ->continuation.resume(data) }
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