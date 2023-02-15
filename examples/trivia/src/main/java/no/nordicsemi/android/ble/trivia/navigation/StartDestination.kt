package no.nordicsemi.android.ble.trivia.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.trivia.view.StartScreen
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val StartDestination = createSimpleDestination(NavigationConst.START)
private val Start = defineDestination(StartDestination) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    StartScreen(
        onServerNavigation = {viewModel.navigateTo(ServerDestination)},
        onClientNavigation =  {viewModel.navigateTo(ClientDestination)},
    )
}
val StartScreenDestination = Start