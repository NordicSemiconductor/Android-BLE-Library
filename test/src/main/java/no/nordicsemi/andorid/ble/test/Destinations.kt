package no.nordicsemi.andorid.ble.test

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.andorid.ble.test.client.view.ClientScreen
import no.nordicsemi.andorid.ble.test.server.view.ServerScreen
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val StartDestination = createSimpleDestination("start")
val ClientDestination = createSimpleDestination("client-destination")
val ServerDestination = createSimpleDestination("server-destination")

@RequiresApi(Build.VERSION_CODES.O)
val Destinations = listOf(
    defineDestination(StartDestination) {
        val viewModel: SimpleNavigationViewModel = hiltViewModel()
        StartScreen(onNavigation = { viewModel.navigateTo(it) })
    },
    defineDestination(ClientDestination) { ClientScreen() },
    defineDestination(ServerDestination) { ServerScreen() }
)