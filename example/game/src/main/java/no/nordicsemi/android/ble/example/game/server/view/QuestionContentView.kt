package no.nordicsemi.android.ble.example.game.server.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.server.model.htmlTextMapper
import no.nordicsemi.android.ble.example.game.server.data.ColorState
import no.nordicsemi.android.ble.example.game.server.data.DisplayAnswer
import no.nordicsemi.android.common.theme.NordicTheme

/**
 * Shows questions and list of answers.
 */
@Composable
fun QuestionContentView(
    question: String?,
    answers: List<DisplayAnswer>,
    ticks: Long,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        question?.let {
            TimerView(
                key = it,
                duration = ticks,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
        Text(
            text = htmlTextMapper(question),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        LazyColumn {
            items(answers) { answer ->
                Text(
                    modifier = modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = answer.isSelected,
                            onClick = { onAnswerSelected(answer.id) },
                            enabled = answer.enableSelection
                        )
                        .background(
                            color = answer.color.color(),
                        )
                        .padding(16.dp),
                    text = htmlTextMapper(answer.text)
                )
            }
        }
    }
}

@Preview
@Composable
private fun QuestionContentView_Preview() {
    NordicTheme {
        QuestionContentView(
            question =
            "How are you?",
            ticks = 4000,
            modifier = Modifier.fillMaxWidth(),
            onAnswerSelected = {},
            answers = listOf(
                DisplayAnswer(1, "Excellent",
                    isSelected = false,
                    enableSelection = true,
                    color = ColorState.NOT_SELECTED_AND_TIMER_RUNNING
                ),
                DisplayAnswer(1, "Good",
                    isSelected = true,
                    enableSelection = true,
                    color = ColorState.SELECTED_AND_TIMER_RUNNING
                ),
                DisplayAnswer(1, "Ok",
                    isSelected = false,
                    enableSelection = true,
                    color = ColorState.NOT_SELECTED_AND_TIMER_RUNNING
                ),
                DisplayAnswer(1, "Bad",
                    isSelected = false,
                    enableSelection = true,
                    color = ColorState.NOT_SELECTED_AND_TIMER_RUNNING
                ),

                )
        )
    }
}