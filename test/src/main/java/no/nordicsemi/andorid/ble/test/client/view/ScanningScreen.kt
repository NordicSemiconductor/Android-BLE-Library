package no.nordicsemi.andorid.ble.test.client.view

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.andorid.ble.test.R
import no.nordicsemi.andorid.ble.test.client.viewmodel.ClientViewModel
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.permission.RequireLocation
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@Composable
fun ScanningScreen() {
    Column {
        NordicAppBar(text = stringResource(id = R.string.scanning))
        RequireBluetooth {
            RequireLocation {
                val clientViewModel: ClientViewModel = hiltViewModel()
                val bluetoothDevice by clientViewModel.bluetoothDevice.collectAsState()
                bluetoothDevice?.let {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = it.name ?: "No name",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = it.address)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ScanningScreenPreview() {
    NordicTheme {
        ScanningScreen()
    }
}