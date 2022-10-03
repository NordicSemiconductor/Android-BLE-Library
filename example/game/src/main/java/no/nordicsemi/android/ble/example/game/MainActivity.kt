package no.nordicsemi.android.ble.example.game

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.ble.example.game.navigation.AdvertisingDestinations
import no.nordicsemi.android.ble.example.game.server.viewmodel.AdvertisingViewModel
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.navigation.NavigationView
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme {
                Surface {
                    RequireBluetooth {
                        NavigationView(destinations = AdvertisingDestinations)

                    }
                }
            }
        }
    }
    companion object {
        val Main = DestinationId(NavigationConst.HOME)
    }
}
@Preview
@Composable
fun AdvertisingScreen(
    navigationManager: NavigationManager? = null
) {
    val advertisingViewModel: AdvertisingViewModel = hiltViewModel()
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Starting Advertising...")
        Spacer(modifier = Modifier.padding(16.dp))
        Box(
            modifier = Modifier.clickable {
                advertisingViewModel.startAdvertising()
                                          },
            contentAlignment = Alignment.Center
            ) {
            Text(text = "Start Advertising")
        }
    }
}


