package no.nordicsemi.andorid.ble.test

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onNavigation: (DestinationId<Unit, *>) -> Unit,
) {
    Column {
        NordicAppBar(text = stringResource(id = R.string.welcome_message))
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = stringResource(id = R.string.select_one))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(onClick = { onNavigation(ServerDestination) }) {
                    Text(text = stringResource(id = R.string.advertise))
                }
                Button(onClick = { onNavigation(ClientDestination) }) {
                    Text(text = stringResource(id = R.string.scanning))
                }
            }
        }
    }
}