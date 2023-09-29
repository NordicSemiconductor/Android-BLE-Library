package no.nordicsemi.andorid.ble.test.server.viewmodel

sealed interface TestState

internal data class WaitingForClient(val connectedClient: Int) : TestState
