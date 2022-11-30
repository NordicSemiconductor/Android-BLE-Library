package no.nordicsemi.andorid.ble.test.server.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.andorid.ble.test.R
import no.nordicsemi.andorid.ble.test.server.viewmodel.ServerViewModel
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisingScreen() {
    Column {
        NordicAppBar(text = stringResource(id = R.string.advertise))
        RequireBluetooth {
            val serverViewModel: ServerViewModel = hiltViewModel()
            Text(text = "Advertising...") // todo: text as a placeholder, change it to state
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvertisingScreenPreview() {
    NordicTheme {
        AdvertisingScreen()
    }
}