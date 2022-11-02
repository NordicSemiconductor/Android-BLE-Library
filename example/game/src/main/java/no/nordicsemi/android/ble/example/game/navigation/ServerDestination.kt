package no.nordicsemi.android.ble.example.game.navigation

import no.nordicsemi.android.ble.example.game.MainActivity
import no.nordicsemi.android.ble.example.game.server.ServerScreen
import no.nordicsemi.android.common.navigation.ComposeDestination
import no.nordicsemi.android.common.navigation.ComposeDestinations

private val Server = ComposeDestination(MainActivity.Server) { navigationManager ->
    ServerScreen(navigationManager)
}
val ServerDestinations = ComposeDestinations(Server)