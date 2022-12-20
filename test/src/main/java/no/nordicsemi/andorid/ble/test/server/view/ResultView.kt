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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.andorid.ble.test.server.data.TestCase
import no.nordicsemi.android.common.theme.NordicTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultView(testItems: List<TestCase>, icon: ImageVector?, iconColor: Color) {
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
        items(items = testItems) { items ->
            Row(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = items.testName,
                    modifier = Modifier.weight(1f)
                )
                icon?.let {
                    Icon(
                        imageVector = it, contentDescription = null,
                        tint = iconColor,
                    )
                }

            }
        }

    }
}

@Preview
@Composable
fun ResultViewPreview() {
    NordicTheme {
        ResultView(
            testItems = listOf(
                TestCase("Test 1", true),
                TestCase("Test 2", true),
                TestCase("Test 3", false),
                TestCase("Test 4", false),
            ),
            icon = Icons.Default.Check,
            iconColor = Color.Green
        )
    }
}