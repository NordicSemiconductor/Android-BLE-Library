package no.nordicsemi.android.ble.ktx.state

@Suppress("unused")
sealed class BondState {

    /** The device was not connected or is not bonded. */
    object NotBonded: BondState()

    /** Bonding has started. */
    object Bonding: BondState()

    /** The device is bonded. */
    object Bonded: BondState()

    /** Whether bonding was established. */
    val isBonded: Boolean
        get() = this is Bonded
}