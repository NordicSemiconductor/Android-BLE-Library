package no.nordicsemi.andorid.ble.test.scanner.view

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.andorid.ble.test.R
import no.nordicsemi.andorid.ble.test.scanner.viewmodel.ClientViewModel
import no.nordicsemi.andorid.ble.test.advertiser.view.LoadingView
import no.nordicsemi.andorid.ble.test.scanner.viewmodel.ClientViewState
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.permission.RequireLocation
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScanningScreen() {
    Column {
        NordicAppBar(text = stringResource(id = R.string.scanner))
        RequireBluetooth {
            RequireLocation {
                val clientViewModel: ClientViewModel = hiltViewModel()
                val scanningState by clientViewModel.scanningStateView.collectAsState()

                when(scanningState.state){
                    ConnectionState.Connecting -> ConnectingView()
                    ConnectionState.Initializing -> InitializingView()
                    ConnectionState.Ready -> ReadyView(scanningState)
                    is ConnectionState.Disconnected -> DisconnectedView()
                    else -> LoadingView()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun ReadyView(scanningState: ClientViewState ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp)
        ) {
            stickyHeader {
                Surface(shadowElevation = 4.dp) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Features",
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "Pass/Fail")
                    }
                }
            }
            items(items = scanningState.testItems ) { items ->
                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = items.testName,
                        modifier = Modifier.weight(1f)
                    )
                    scanningState.getIcon()?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = scanningState.color,
                        )
                    }
                }
            }

        }
}

@Preview(showBackground = true)
@Composable
fun ReadyViewPreview() {
//    NordicTheme {
//        ReadyView() ///    }
}

@Composable
fun DisconnectedView(){
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

@Preview(showBackground = true)
@Composable
fun DisconnectedViewPreview() {
    NordicTheme {
        DisconnectedView()
    }
}

@Composable
fun ConnectingView(){
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(
            text = stringResource(id = R.string.connecting),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectingViewPreview() {
    NordicTheme {
        ConnectingView()
    }
}

@Composable
fun InitializingView(){
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(
            text = stringResource(id = R.string.initializing),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InitializingViewPreview() {
    NordicTheme {
        InitializingView()
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