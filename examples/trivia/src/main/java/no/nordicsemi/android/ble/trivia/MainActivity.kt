package no.nordicsemi.android.ble.trivia

import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ble.trivia.navigation.ClientDestinations
import no.nordicsemi.android.ble.trivia.navigation.NavigationConst
import no.nordicsemi.android.ble.trivia.navigation.ServerDestinations
import no.nordicsemi.android.ble.trivia.navigation.StartScreenDestination
import no.nordicsemi.android.common.navigation.NavigationView
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NordicTheme {
                NavigationView(
                    destinations = StartScreenDestination
                            + ServerDestinations
                            + ClientDestinations
                )
            }
        }
    }

    companion object {
        val Start = createSimpleDestination(NavigationConst.START)
        val Server = createSimpleDestination(NavigationConst.SERVER)
        val Client = createSimpleDestination(NavigationConst.CLIENT)
    }
}


