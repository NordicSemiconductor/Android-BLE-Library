@file:Suppress("unused")

package no.nordicsemi.android.ble.ktx

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.ble.*
import no.nordicsemi.android.ble.data.DataMerger
import no.nordicsemi.android.ble.data.DataSplitter
import no.nordicsemi.android.ble.data.DefaultMtuSplitter

/**
 * The upload or download progress indication.
 *
 * @since 2.4.0
 * @property index The 0-based index of the packet. Only the packets that passed the filter
 *                 will be reported. As the number of expected packets is not know, it is up to the
 *                 application to calculate the real progress based on the index and data length.
 * @property data  The latest received packet as it was sent by the remote device.
 */
data class ProgressIndication(val index: Int, val data: ByteArray?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProgressIndication

        if (index != other.index) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Adds a merger that will be used to merge multiple packets into a single Data.
 * The merger may modify each packet if necessary.
 *
 * The returned flow will be notified each time a new packet is received.
 *
 * @return The flow with progress indications.
 * @since 2.4.0
 */
fun ReadRequest.mergeWithProgressFlow(merger: DataMerger): Flow<ProgressIndication> {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    // Create a temporary callback that will be used to emit progress.
    var callback: ((ProgressIndication) -> Unit)? = null
    // Set the merger, which will invoke the temporary callback on progress.
    // The merger must be called here, not in the callbackFlow.
    merge(merger)  { _, data, index ->
        // The temporary callback will be set in the callbackFlow below.
        callback?.invoke(ProgressIndication(index, data))
    }
    // Return the callback flow. It will be closed when the request is complete or has failed.
    return callbackFlow {
        callback = { trySend(it) }
        then { close() }
        awaitClose { callback = null }
    }
}

/**
 * Adds a merger that will be used to merge multiple packets into a single Data.
 * The merger may modify each packet if necessary.
 *
 * The returned flow will be notified each time a new packet is received.
 *
 * @return The flow with progress indications.
 * @since 2.4.0
 */
fun WaitForValueChangedRequest.mergeWithProgressFlow(merger: DataMerger): Flow<ProgressIndication> {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    // Create a temporary callback that will be used to emit progress.
    var callback: ((ProgressIndication) -> Unit)? = null
    // Set the merger, which will invoke the temporary callback on progress.
    // The merger must be called here, not in the callbackFlow.
    merge(merger)  { _, data, index ->
        // The temporary callback will be set in the callbackFlow below.
        callback?.invoke(ProgressIndication(index, data))
    }
    // Return the callback flow. It will be closed when the request is complete or has failed.
    return callbackFlow {
        callback = { trySend(it) }
        then { close() }
        awaitClose { callback = null }
    }
}

/**
 * Adds a merger that will be used to merge multiple packets into a single Data.
 * The merger may modify each packet if necessary.
 *
 * The returned flow will be notified each time a new packet is received.
 *
 * @return The flow with progress indications.
 * @since 2.4.0
 */
fun ValueChangedCallback.mergeWithProgressFlow(merger: DataMerger): Flow<ProgressIndication> {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    // Create a temporary callback that will be used to emit progress.
    var callback: ((ProgressIndication) -> Unit)? = null
    // Set the merger, which will invoke the temporary callback on progress.
    // The merger must be called here, not in the callbackFlow.
    merge(merger)  { _, data, index ->
        // The temporary callback will be set in the callbackFlow below.
        callback?.invoke(ProgressIndication(index, data))
    }
    // Return the callback flow. It will be closed when the request is complete or has failed.
    return callbackFlow {
        callback = { trySend(it) }
        then { close() }
        awaitClose { callback = null }
    }
}

/**
 * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
 * bytes long packets.
 *
 * The returned flow will be notified each time a new packet is sent.
 *
 * @return The flow with progress indications.
 * @since 2.4.0
 */
fun WriteRequest.splitWithProgressFlow(): Flow<ProgressIndication> = splitWithProgressFlow(DefaultMtuSplitter())

/**
 * Adds a splitter that will be used to cut given data into multiple packets.
 * The splitter may modify each packet if necessary, i.e. add a flag indicating first packet,
 * continuation or the last packet.
 *
 * The returned flow will be notified each time a new packet is sent.
 *
 * @return The flow with progress indications.
 * @since 2.4.0
 */
fun WriteRequest.splitWithProgressFlow(splitter: DataSplitter): Flow<ProgressIndication> {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    // Create a temporary callback that will be used to emit progress.
    var callback: ((ProgressIndication) -> Unit)? = null
    // Set the splitter, which will invoke the temporary callback on progress.
    // The splitter must be called here, not in the callbackFlow.
    split(splitter) { _, data, index ->
        // The temporary callback will be set in the callbackFlow below.
        callback?.invoke(ProgressIndication(index, data))
    }
    // Return the callback flow. It will be closed when the request is complete or has failed.
    return callbackFlow {
        callback = { trySend(it) }
        then { close() }
        awaitClose { callback = null }
    }
}

/**
 * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
 * bytes long packets.
 *
 * The returned flow will be notified each time a new packet is sent.
 *
 * @return The flow with progress indications.
 * @since 2.4.0
 */
fun WaitForReadRequest.splitWithProgressFlow(): Flow<ProgressIndication> = splitWithProgressFlow(DefaultMtuSplitter())

/**
 * Adds a splitter that will be used to cut given data into multiple packets.
 * The splitter may modify each packet if necessary, i.e. add a flag indicating first packet,
 * continuation or the last packet.
 *
 * The returned flow will be notified each time a new packet is sent.
 *
 * @return The flow with progress indications.
 * @since 2.4.0
 */
fun WaitForReadRequest.splitWithProgressFlow(splitter: DataSplitter): Flow<ProgressIndication> {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    // Set the splitter, which will invoke the temporary callback on progress.
    // The splitter must be called here, not in the callbackFlow.
    var callback: ((ProgressIndication) -> Unit)? = null
    // Set the splitter, which invokes the temporary callback.
    split(splitter) { _, data, index ->
        // The temporary callback will be set in the callbackFlow below.
        callback?.invoke(ProgressIndication(index, data))
    }
    // Return the callback flow. It will be closed when the request is complete or has failed.
    return callbackFlow {
        callback = { trySend(it) }
        then { close() }
        awaitClose { callback = null }
    }
}