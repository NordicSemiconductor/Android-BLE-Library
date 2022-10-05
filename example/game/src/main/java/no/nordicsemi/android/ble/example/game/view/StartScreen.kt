package no.nordicsemi.android.ble.example.game.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.MainActivity
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.theme.view.NordicAppBar

@Composable
fun StartScreen(
    navigationManager: NavigationManager,
) {
    Column {
        NordicAppBar(
            text = stringResource(id = R.string.app_name)
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.start_game_description)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        navigationManager.navigateTo(MainActivity.Server)
                    }
                ) {
                    Text(text = stringResource(id = R.string.start_game))
                }

                Spacer(modifier = Modifier.padding(16.dp))

                Button(
                    onClick = {
                        navigationManager.navigateTo(MainActivity.Client)
                    }
                ) {
                    Text(text = stringResource(id = R.string.join_game))
                }
            }
        }
    }
}