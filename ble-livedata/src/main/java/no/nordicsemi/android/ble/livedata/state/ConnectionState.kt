package no.nordicsemi.android.ble.livedata.state

import no.nordicsemi.android.ble.annotation.DisconnectionReason
import no.nordicsemi.android.ble.observer.ConnectionObserver

@Suppress("unused")
sealed class ConnectionState(val state: State) {
    /** The connection state. This can be used in <i>switch</i> in Java. */
    enum class State {
        CONNECTING, INITIALIZING, READY, DISCONNECTING, DISCONNECTED
    }

    /** A connection to the device was initiated. */
    object Connecting: ConnectionState(State.CONNECTING)

    /** The device has connected and begun service discovery and initialization. */
    object Initializing: ConnectionState(State.INITIALIZING)

    /** The initialization is complete, and the device is ready to use. */
    object Ready: ConnectionState(State.READY)

    /** The disconnection was initiated. */
    object Disconnecting: ConnectionState(State.DISCONNECTING)

    /**
     * The device disconnected or failed to connect.
     *
     * @param reason The reason of disconnection.
     */
    data class Disconnected(@DisconnectionReason val reason: Int): ConnectionState(State.DISCONNECTED) {
        /** Whether the device, that was connected using auto connect, has disconnected. */
        val isLinkLoss: Boolean
            get() = reason == ConnectionObserver.REASON_LINK_LOSS

        /** Whether at least one required service was not found. */
        val isNotSupported: Boolean
            get() = reason == ConnectionObserver.REASON_NOT_SUPPORTED

        /** Whether the connection timed out. */
        val isTimeout: Boolean
            get() = reason == ConnectionObserver.REASON_TIMEOUT
    }

    /**
     * Whether the target device is connected, or not.
     */
    val isConnected: Boolean
        get() = this is Initializing || this is Ready

    /**
     * Whether the target device is ready to use.
     */
    val isReady: Boolean
        get() = this is Ready

}