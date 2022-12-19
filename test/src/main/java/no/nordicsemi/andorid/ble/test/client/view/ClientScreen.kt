package no.nordicsemi.andorid.ble.test.client.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.andorid.ble.test.R
import no.nordicsemi.andorid.ble.test.client.viewmodel.ClientViewModel
import no.nordicsemi.andorid.ble.test.server.view.LoadingView
import no.nordicsemi.andorid.ble.test.server.view.ResultView
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.permission.RequireLocation
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClientScreen() {
    Column {
        NordicAppBar(text = stringResource(id = R.string.scanner))
        RequireBluetooth {
            RequireLocation {
                val clientViewModel: ClientViewModel = hiltViewModel()
                val clientViewState by clientViewModel.clientViewState.collectAsState()

                when (clientViewState.state) {
                    ConnectionState.Connecting -> ConnectingView()
                    ConnectionState.Initializing -> InitializingView()
                    ConnectionState.Ready -> ResultView(
                        clientViewState.testItems,
                        clientViewState.getIcon(),
                        clientViewState.color
                    )
                    is ConnectionState.Disconnected -> DisconnectedView()
                    else -> LoadingView()
                }
            }
        }
    }
}