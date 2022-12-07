package no.nordicsemi.android.ble.trivia.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.trivia.MainActivity
import no.nordicsemi.android.ble.trivia.view.StartScreen
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

private val Start = defineDestination(MainActivity.Start) {
    val viewModel: SimpleNavigationViewModel = hiltViewModel()

    StartScreen(
        onNavigation = { viewModel.navigateTo(it) }
    )
}
val StartScreenDestination = Start