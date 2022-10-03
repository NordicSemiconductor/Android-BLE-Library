package no.nordicsemi.android.ble.example.game.navigation

import no.nordicsemi.android.ble.example.game.AdvertisingScreen
import no.nordicsemi.android.ble.example.game.MainActivity
import no.nordicsemi.android.common.navigation.ComposeDestination
import no.nordicsemi.android.common.navigation.ComposeDestinations

private val Advertiser =  ComposeDestination(MainActivity.Main) { navigationManager ->
    AdvertisingScreen(navigationManager)
}
val AdvertisingDestinations = ComposeDestinations ( Advertiser )