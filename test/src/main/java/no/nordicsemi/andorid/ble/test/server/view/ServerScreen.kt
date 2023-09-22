package no.nordicsemi.andorid.ble.test.server.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.andorid.ble.test.R
import no.nordicsemi.andorid.ble.test.server.viewmodel.ServerViewModel
import no.nordicsemi.andorid.ble.test.server.viewmodel.WaitingForClient
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen() {
    Column {
        NordicAppBar(text = stringResource(id = R.string.advertiser))
        RequireBluetooth {
            val serverViewModel: ServerViewModel = hiltViewModel()
            val serverViewState by serverViewModel.serverViewState.collectAsStateWithLifecycle()

            when (val currentState = serverViewState.state) {
                is WaitingForClient ->
                    if (currentState.connectedClient == 0) {
                        WaitingForScannersView()
                    } else {
                        ResultView(results = serverViewState.resultList)
                    }
            }
        }
    }
}