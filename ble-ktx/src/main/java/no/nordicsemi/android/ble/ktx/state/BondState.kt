package no.nordicsemi.android.ble.ktx.state

@Suppress("unused")
sealed class BondState {

    /** The device was not connected or is not bonded. */
    data object NotBonded: BondState()

    /** Bonding has started. */
    data object Bonding: BondState()

    /** The device is bonded. */
    data object Bonded: BondState()

    /** Whether bonding was established. */
    val isBonded: Boolean
        get() = this is Bonded
}