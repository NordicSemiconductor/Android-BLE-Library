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
 * @property index The 0-based index of the packet. The packet will be app
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
 */
fun ReadRequest.mergeWithProgressFlow(merger: DataMerger): Flow<ProgressIndication> = callbackFlow {
    merge(merger)  { _, data, index -> trySend(ProgressIndication(index, data)) }
    awaitClose {
        merge(merger) // remove the progress listener, but keep the merger
    }
}

/**
 * Adds a merger that will be used to merge multiple packets into a single Data.
 * The merger may modify each packet if necessary.
 *
 * The returned flow will be notified each time a new packet is received.
 *
 * @return The flow with progress indications.
 */
fun WaitForValueChangedRequest.mergeWithProgressFlow(merger: DataMerger): Flow<ProgressIndication> = callbackFlow {
    merge(merger)  { _, data, index -> trySend(ProgressIndication(index, data)) }
    awaitClose {
        merge(merger) // remove the progress listener, but keep the merger
    }
}

/**
 * Adds a merger that will be used to merge multiple packets into a single Data.
 * The merger may modify each packet if necessary.
 *
 * The returned flow will be notified each time a new packet is received.
 *
 * @return The flow with progress indications.
 */
fun ValueChangedCallback.mergeWithProgressFlow(merger: DataMerger): Flow<ProgressIndication> = callbackFlow {
    merge(merger)  { _, data, index -> trySend(ProgressIndication(index, data)) }
    awaitClose {
        merge(merger) // remove the progress listener, but keep the merger
    }
}

/**
 * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
 * bytes long packets.
 *
 * The returned flow will be notified each time a new packet is sent.
 *
 * @return The flow with progress indications.
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
 */
fun WriteRequest.splitWithProgressFlow(splitter: DataSplitter): Flow<ProgressIndication> = callbackFlow {
    split(splitter)  { _, data, index -> trySend(ProgressIndication(index, data)) }
    awaitClose {
        split(splitter) // remove the progress listener, but keep the merger
    }
}

/**
 * Adds a default MTU splitter that will be used to cut given data into at-most MTU-3
 * bytes long packets.
 *
 * The returned flow will be notified each time a new packet is sent.
 *
 * @return The flow with progress indications.
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
 */
fun WaitForReadRequest.splitWithProgressFlow(splitter: DataSplitter): Flow<ProgressIndication> = callbackFlow {
    split(splitter)  { _, data, index -> trySend(ProgressIndication(index, data)) }
    awaitClose {
        split(splitter) // remove the progress listener, but keep the merger
    }
}