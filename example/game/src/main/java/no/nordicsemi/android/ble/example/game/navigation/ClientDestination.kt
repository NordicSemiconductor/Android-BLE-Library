package no.nordicsemi.android.ble.example.game.navigation

import no.nordicsemi.android.ble.example.game.MainActivity
import no.nordicsemi.android.ble.example.game.client.ClientScreen
import no.nordicsemi.android.common.navigation.ComposeDestination
import no.nordicsemi.android.common.navigation.ComposeDestinations

private val Client = ComposeDestination(MainActivity.Client) { navigationManager ->
    ClientScreen(navigationManager)
}
val ClientDestinations = ComposeDestinations(Client)