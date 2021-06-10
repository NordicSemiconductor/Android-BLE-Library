package no.nordicsemi.android.ble.ktx

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.ble.ValueChangedCallback
import no.nordicsemi.android.ble.data.Data

/**
 * Represents the value changed callback as a cold flow of bytes.
 * @return The flow.
 */
@ExperimentalCoroutinesApi
fun ValueChangedCallback.asFlow(): Flow<Data> = callbackFlow {
    // TODO There is no way to close the Flow.
    //      Flow could be closed when the device gets disconnected, teh callback is removed or
    //      notifications / indications are stopped.
    with { _, data ->
        trySend(data)
    }
}