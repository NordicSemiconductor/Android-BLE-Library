package no.nordicsemi.andorid.ble.test.advertiser.viewmodel

sealed interface TestState

internal data class WaitingForClient(val connectedClient: Int) : TestState
