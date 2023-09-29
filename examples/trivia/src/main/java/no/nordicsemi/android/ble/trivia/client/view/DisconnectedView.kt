package no.nordicsemi.android.ble.trivia.client.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.trivia.R

@Composable
fun DisconnectedView(reason: ConnectionState.Disconnected.Reason) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.disconnected),
            )
            Text(
                text = reason.name(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
fun DisconnectedView_Preview(){
    DisconnectedView(ConnectionState.Disconnected.Reason.TERMINATE_LOCAL_HOST)
}

private fun ConnectionState.Disconnected.Reason.name(): String = when (this) {
    ConnectionState.Disconnected.Reason.TERMINATE_PEER_USER -> "Terminated by peer user"
    ConnectionState.Disconnected.Reason.TERMINATE_LOCAL_HOST -> "Terminated by local host"
    ConnectionState.Disconnected.Reason.CANCELLED -> "Cancelled"
    ConnectionState.Disconnected.Reason.LINK_LOSS -> "Link loss"
    ConnectionState.Disconnected.Reason.NOT_SUPPORTED -> "Not supported"
    ConnectionState.Disconnected.Reason.TIMEOUT -> "Timeout"
    ConnectionState.Disconnected.Reason.UNKNOWN -> "Unknown"
    else -> "Unknown reason"
}