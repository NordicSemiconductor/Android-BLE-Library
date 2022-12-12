package no.nordicsemi.andorid.ble.test.advertiser.view

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
import no.nordicsemi.andorid.ble.test.advertiser.viewmodel.ServerViewModel
import no.nordicsemi.andorid.ble.test.advertiser.viewmodel.ServerViewState
import no.nordicsemi.andorid.ble.test.advertiser.viewmodel.WaitingForClient
import no.nordicsemi.android.common.permission.RequireBluetooth
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertisingScreen() {
    Column {
        NordicAppBar(text = stringResource(id = R.string.advertiser))
        RequireBluetooth {
            val serverViewModel: ServerViewModel = hiltViewModel()
            val serverViewState by serverViewModel.serverViewState.collectAsState()
            when (val currentState = serverViewState.state) {
                is WaitingForClient ->
                    if (currentState.connectedClient == 0) {
                        WaitingForScannersView()
                    } else {
                        TestResultView(serverViewState)
                    }
            }
        }
    }
}

@Composable
fun WaitingForScannersView() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(
            text = stringResource(id = R.string.waiting_for_scanner),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingForClientsViewPreview() {
    NordicTheme {
        WaitingForScannersView()
    }
}

@Composable
fun LoadingView() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(
            text = stringResource(id = R.string.loading),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingViewPreview() {
    NordicTheme {
        LoadingView()
    }
}

data class TestEvent(
    val testName: String,
    val isPassed: Boolean
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TestResultView(serverViewState: ServerViewState) {
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
        } }
        items(items = serverViewState.testItems) { items->
            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = items.testName,
                    modifier = Modifier.weight(1f)
                )
                serverViewState.getIcon()?.let {
                    Icon(
                        imageVector = it, contentDescription = null,
                        tint =  serverViewState.color,
                    )
                }

            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun TestingStaticsViewPreview() {
    NordicTheme {
//        TestResultView
    }
}