package no.nordicsemi.android.ble.example.game.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.MainActivity
import no.nordicsemi.android.ble.example.game.server.ServerScreen
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

private val Server = defineDestination(MainActivity.Server) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    ServerScreen(
        onNavigationUp = { viewModel.navigateUp() }
    )
}
val ServerDestinations = Server
