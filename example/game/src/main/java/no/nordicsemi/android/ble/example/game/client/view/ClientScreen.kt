package no.nordicsemi.android.ble.example.game.client.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import no.nordicsemi.android.ble.example.game.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.client.viewmodel.ClientViewModel
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

@Composable
fun ClientScreen(
    navigationManager: NavigationManager,
) {
    Column {
        NordicAppBar(
            text = stringResource(id = R.string.client),
            onNavigationButtonClick = {
                navigationManager.navigateUp()
            }
        )

        RequireBluetooth {
            val scanningViewModel: ClientViewModel = hiltViewModel()
            val state by scanningViewModel.state.collectAsState()

            when (state) {
                ConnectionState.Connecting -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.scanning_for_server),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                ConnectionState.Ready -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.connected),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is ConnectionState.Disconnected -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.disconnected),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {}
            }

        }
    }
}