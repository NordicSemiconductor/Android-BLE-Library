package no.nordicsemi.android.ble.ktx

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import no.nordicsemi.android.ble.ValueChangedCallback
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.response.ReadResponse

/**
 * Represents the value changed callback as a cold flow of bytes.
 *
 * Usage:
 *
 *     val hrmMeasurementsData = enableNotifications(hrmCharacteristic).asFlow()
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

/**
 * Represents the value changed callback as a cold flow of responses of returned type.
 *
 * Usage:
 *
 *     val hrmMeasurementsData: Flow<HeartRateMeasurementResponse> = enableNotifications(hrmCharacteristic).asResponseFlow()
 * @return The flow.
 */
@ExperimentalCoroutinesApi
inline fun <reified T: ReadResponse> ValueChangedCallback.asResponseFlow(): Flow<T> = callbackFlow {
    with { device, data ->
        trySend(T::class.java.newInstance().apply { onDataReceived(device, data) })
    }
    awaitClose {
        // There's no way to unregister the callback from here.
        with { _, _ -> }
    }
}

/**
 * Represents the value changed callback as a cold flow of responses of returned type.
 * Invalid values, which could not be parsed to the type, are ignored.
 *
 * Usage:
 *
 *     val hrmMeasurementsData: Flow<HeartRateMeasurementResponse> = enableNotifications(hrmCharacteristic).asValidResponseFlow()
 * @return The flow.
 */
@ExperimentalCoroutinesApi
inline fun <reified T: ProfileReadResponse> ValueChangedCallback.asValidResponseFlow(): Flow<T> = callbackFlow {
    with { device, data ->
        T::class.java.newInstance()
            .apply { onDataReceived(device, data) }
            .takeIf { it.isValid }
            ?.let { trySend(it) }
    }
    awaitClose {
        // There's no way to unregister the callback from here.
        with { _, _ -> }
    }
}