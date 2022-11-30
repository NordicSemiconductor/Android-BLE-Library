package no.nordicsemi.andorid.ble.test

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.andorid.ble.test.client.view.ScanningScreen
import no.nordicsemi.andorid.ble.test.server.view.AdvertisingScreen
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel

val StartDestination = createSimpleDestination("start")
val ClientDestination = createSimpleDestination("scan-destination")
val ServerDestination = createSimpleDestination("advertise-destination")

@RequiresApi(Build.VERSION_CODES.O)
val Destinations = listOf(
    defineDestination(StartDestination) {
        val viewModel: SimpleNavigationViewModel = hiltViewModel()
        StartScreen(onNavigation = { viewModel.navigateTo(it) })
    },
    defineDestination(ClientDestination) { ScanningScreen() },
    defineDestination(ServerDestination) { AdvertisingScreen() }
)


