package no.nordicsemi.android.ble.trivia.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.trivia.server.ServerScreen
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val ServerDestination = createSimpleDestination(NavigationConst.SERVER)
private val Server = defineDestination(ServerDestination) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    ServerScreen(
        onNavigationUp = { viewModel.navigateUp() }
    )
}
val ServerDestinations = Server