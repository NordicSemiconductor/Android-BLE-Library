package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.client.view.QuestionScreenClient
import no.nordicsemi.android.ble.example.game.quiz.repository.Answer
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.timer.ShowTimer
import no.nordicsemi.android.ble.example.game.timer.Timer
import no.nordicsemi.android.common.theme.NordicTheme

@Composable
fun QuestionContent(
    question: Question,
    selectedAnswerId: Int?,
    correctAnswerId: Int?,
    ticks: Long,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowTimer(
        ticks = ticks,
        progress = ticks.toFloat() / Timer.TOTAL_TIME,
        modifier = modifier,
    )
    val isTimerRunning = ticks > 0

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = question.question
                .replace("&quot;", "'")
                .replace("&#039;", "'")
                .replace("&ouml;","ö"),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        question.answers.forEach { answer ->
            Text(
                modifier = modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = answer.id == selectedAnswerId,
                        onClick = { onAnswerSelected(answer.id) },
                        enabled = isTimerRunning && selectedAnswerId == null
                    )
                    .background(
                        color = Color(
                            isCorrect = answer.id == correctAnswerId,
                            isSelected = answer.id == selectedAnswerId,
                            isTimerRunning = isTimerRunning,
                        ),
                    )
                    .padding(16.dp),
                text = answer.text
                    .replace("&quot;", "'")
                    .replace("&#039;", "'")
            )
        }
    }
}

@Composable
fun Color(
    isCorrect: Boolean,
    isSelected: Boolean,
    isTimerRunning: Boolean,
): Color = when {
    isCorrect -> Color.Green
    isSelected && isTimerRunning -> MaterialTheme.colorScheme.secondary
    isTimerRunning or !isSelected -> Color.Unspecified
    else -> Color.Red
}

@Preview
@Composable
private fun QuestionScreenClient_Preview() {
    NordicTheme {
        QuestionScreenClient(
            question = Question("How are you?", listOf(
                Answer("Good", 0),
                Answer("OK", 1),
                Answer("Bad", 2),
                Answer("Are you joking?", 3),
            )),
            correctAnswerId = 0,
            selectedAnswerId = 3,
            ticks = 4000,
            modifier = Modifier.fillMaxWidth(),
            onAnswerSelected = {}
        )
    }
}