package no.nordicsemi.android.ble.livedata.state

@Suppress("unused")
sealed class BondState(val state: State) {
    /** The bonding state. This can be used in <i>switch</i> in Java. */
    enum class State {
        NOT_BONDED, BONDING, BONDED
    }

    /** The device was not connected or is not bonded. */
    object NotBonded: BondState(State.NOT_BONDED)

    /** Bonding has started. */
    object Bonding: BondState(State.BONDING)

    /** The device is bonded. */
    object Bonded: BondState(State.BONDED)

    /** Whether bonding was established. */
    val isBonded: Boolean
        get() = this is Bonded
}