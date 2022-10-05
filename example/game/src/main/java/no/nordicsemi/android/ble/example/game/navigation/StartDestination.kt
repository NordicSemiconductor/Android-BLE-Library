package no.nordicsemi.android.ble.example.game.navigation

import no.nordicsemi.android.ble.example.game.MainActivity
import no.nordicsemi.android.ble.example.game.view.StartScreen
import no.nordicsemi.android.common.navigation.ComposeDestination
import no.nordicsemi.android.common.navigation.ComposeDestinations

private val Home = ComposeDestination(MainActivity.Main) { navigationManager ->
    StartScreen(navigationManager)
}
val HomeScreen = ComposeDestinations(Home)