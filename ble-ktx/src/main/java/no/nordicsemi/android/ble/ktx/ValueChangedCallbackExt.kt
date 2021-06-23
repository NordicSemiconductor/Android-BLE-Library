package no.nordicsemi.android.ble.ktx

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
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
    with { _, data ->
        trySend(data)
    }
    awaitClose {
        // There's no way to unregister the callback from here.
        with { _, _ -> }
    }
}