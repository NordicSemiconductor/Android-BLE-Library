package no.nordicsemi.andorid.ble.test.server.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.andorid.ble.test.server.data.Result
import no.nordicsemi.android.common.theme.NordicTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultView(results: List<Result>) {
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
        items(items = results) { items ->
            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = items.name,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(color = items.color)
                )
                Icon(
                    imageVector = items.icon, contentDescription = null,
                    tint = items.color,
                )
            }

        }
    }
}

@Preview
@Composable
fun ResultViewPreview() {
    NordicTheme {
        val results = listOf(
            Result("Test 1", true, Icons.Default.Check,  Color.Green),
            Result("Test 4", false, Icons.Default.Close, Color.Red),
            Result("Test 5", true, Icons.Default.Check,  Color.Green)
        )
        ResultView(results = results)
    }
}