@file:Suppress("unused")

package no.nordicsemi.android.ble.ktx

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
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
 *     val hrmMeasurementsData = setNotificationCallback(hrmCharacteristic).asFlow()  // Flow<Data>
 *
 * Use the [buffer] operator on the resulting flow to specify a user-defined value and to control
 * what happens when data is produced faster than consumed, i.e. to control the back-pressure behavior.
 *
 *      val hrmMeasurementsData = setNotificationCallback(hrmCharacteristic).asFlow().buffer()
 * @return The flow.
 * @since 2.3.0
 */
@ExperimentalCoroutinesApi
fun ValueChangedCallback.asFlow(): Flow<Data> = callbackFlow {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    with { _, data ->
        trySendBlocking(data)
            .onFailure { t -> Log.w("ValueChangeCallback", "Sending data to Flow failed with: $t") }
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
 *     val hrmMeasurementsData: Flow<HeartRateMeasurementResponse> =
 *         setNotificationCallback(hrmCharacteristic)
 *             .asResponseFlow()
 *
 * Use the [buffer] operator on the resulting flow to specify a user-defined value and to control
 * what happens when data is produced faster than consumed, i.e. to control the back-pressure behavior.
 * @return The flow.
 * @since 2.4.0
 */
@ExperimentalCoroutinesApi
inline fun <reified T: ReadResponse> ValueChangedCallback.asResponseFlow(): Flow<T> = callbackFlow {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    with { device, data ->
        val response = T::class.java.getDeclaredConstructor().newInstance()
            .apply { onDataReceived(device, data) }
        trySendBlocking(response)
            .onFailure { t -> Log.w("ValueChangeCallback", "Sending response to Flow failed with: $t") }
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
 *     val hrmMeasurementsData: Flow<HeartRateMeasurementResponse> =
 *         setNotificationCallback(hrmCharacteristic)
 *             .asValidResponseFlow()
 *
 * Use the [buffer] operator on the resulting flow to specify a user-defined value and to control
 * what happens when data is produced faster than consumed, i.e. to control the back-pressure behavior.
 * @return The flow.
 * @since 2.4.0
 */
@ExperimentalCoroutinesApi
inline fun <reified T: ProfileReadResponse> ValueChangedCallback.asValidResponseFlow(): Flow<T> = callbackFlow {
    // Make sure the callbacks are called without unnecessary delay.
    setHandler(null)
    with { device, data ->
        val response = T::class.java.getDeclaredConstructor().newInstance()
            .apply { onDataReceived(device, data) }
        if (response.isValid) {
            trySendBlocking(response)
                .onFailure { t -> Log.w("ValueChangeCallback", "Sending response to Flow failed with: $t") }
        }
    }
    awaitClose {
        // There's no way to unregister the callback from here.
        with { _, _ -> }
    }
}