package no.nordicsemi.android.ble.example.game

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ble.example.game.navigation.ClientDestinations
import no.nordicsemi.android.ble.example.game.navigation.HomeScreen
import no.nordicsemi.android.ble.example.game.navigation.NavigationConst
import no.nordicsemi.android.ble.example.game.navigation.ServerDestinations
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.NavigationView
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme {
                Surface {
                    NavigationView(
                        destinations = HomeScreen
                                + ServerDestinations
                                + ClientDestinations
                    )
                }
            }
        }
    }

    companion object {
        val Main = DestinationId(NavigationConst.HOME)
        val Server = DestinationId(NavigationConst.SERVER)
        val Client = DestinationId(NavigationConst.CLIENT)
    }
}


