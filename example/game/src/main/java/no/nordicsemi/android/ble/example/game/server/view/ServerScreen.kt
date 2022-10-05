package no.nordicsemi.android.ble.example.game.server.view

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
import no.nordicsemi.android.ble.example.game.server.viewmodel.ServerViewModel
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

@Composable
fun ServerScreen(
    navigationManager: NavigationManager,
) {
    Column {
        NordicAppBar(
            text = stringResource(id = R.string.server),
            onNavigationButtonClick = {
                navigationManager.navigateUp()
            }
        )
        RequireBluetooth {
            val serverViewModel: ServerViewModel = hiltViewModel()
            val numberOfClients by serverViewModel.clientsNumber.collectAsState()

            when (numberOfClients) {
                0 -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.looking_for_clients),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    ) {
                        Text(
                            text = "Connected clients: $numberOfClients",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}