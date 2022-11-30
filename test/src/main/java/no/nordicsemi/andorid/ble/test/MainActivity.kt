package no.nordicsemi.andorid.ble.test

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.common.navigation.NavigationView
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NordicTheme {
                NavigationView(destinations = Destinations)

            }
        }
    }
}