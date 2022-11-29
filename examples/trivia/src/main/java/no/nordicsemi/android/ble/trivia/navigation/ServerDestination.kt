package no.nordicsemi.android.ble.trivia.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.trivia.MainActivity
import no.nordicsemi.android.ble.trivia.server.ServerScreen
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

private val Server = defineDestination(MainActivity.Server) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    ServerScreen(
        onNavigationUp = { viewModel.navigateUp() }
    )
}
val ServerDestinations = Server
