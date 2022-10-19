package no.nordicsemi.android.ble.example.game.client.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.quiz.repository.Answer
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.timer.ShowTimer
import no.nordicsemi.android.ble.example.game.timer.TimerViewModel
import no.nordicsemi.android.common.navigation.NavigationManager


@Composable
fun QuestionScreenClient(
    navigationManager: NavigationManager,
    question: Question,
    Answer: Int?
) {
    var selectedAnswerId by mutableStateOf(-1)
    val timerViewModel: TimerViewModel = hiltViewModel()
    val ticks by timerViewModel.ticks.collectAsState()
    val isTimerRunning = ticks > 0

    ShowTimer( ticks, ticks.toFloat() / timerViewModel.ticksTotal )
    LaunchedEffect(key1 = Unit) {
        timerViewModel.startCountDown()
    }

    QuestionContentClient(
        question = question,
        selectedAnswer = selectedAnswerId,
        isTimerRunning = isTimerRunning,
        correctAnswer = Answer,
        onAnswer = { selectedAnswerId = it.id },
        modifier = Modifier
            .fillMaxWidth()
    )
}


@Composable
fun QuestionContentClient(
    question: Question,
    selectedAnswer: Int,
    isTimerRunning: Boolean,
    correctAnswer: Int?,
    onAnswer: (Answer) -> Unit,
    modifier: Modifier,

    ) {
    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp
        )
    ) {
        item {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = question.question,
                    modifier = Modifier
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            SingleChoiceQuestionClient(
                question = question,
                selectedAnswer = selectedAnswer,
                correctAnswer = correctAnswer,
                enableQuestionSelection = isTimerRunning,
                onAnswerSelected = { answer -> onAnswer(answer) },
                modifier = Modifier.fillParentMaxWidth()
            )
        }

    }
}

@Composable
fun SingleChoiceQuestionClient(
    question: Question,
    selectedAnswer: Int,
    enableQuestionSelection: Boolean,
    correctAnswer: Int?,
    onAnswerSelected: (Answer) -> Unit, // TODO: Check if this is needed or not, it looks like this is not needed
    modifier: Modifier = Modifier,

    ) {
    val multipleOptions = question.answers

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(selectedAnswer) }

    Column(modifier = modifier) {
        multipleOptions.forEach { answer ->
            val onClickHandle = {
                onOptionSelected(answer.id)
                onAnswerSelected(answer)
            }

            val optionSelected = answer.id == selectedOption

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = optionSelected,
                        onClick = onClickHandle,
                        enabled = enableQuestionSelection
                    )
                    .background(
                        color = checkColor(
                            isTimerRunning = enableQuestionSelection,
                            selectedOption = selectedOption,
                            optionSelected = optionSelected,
                            answerId = answer.id,
                            correctAnswer = correctAnswer
                        ),
                        shape = RectangleShape
                    )
                    .padding(
                        vertical = 16.dp,
                        horizontal = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.weight(4f),
                    text = answer.text
                )
            }
        }
    }
 }

@Composable
fun checkColor(
    isTimerRunning: Boolean,
    selectedOption: Int,
    optionSelected: Boolean,
    answerId: Int,
    correctAnswer: Int?
): Color {
    return if (optionSelected) {
        when (isTimerRunning) {
            true -> MaterialTheme.colorScheme.secondary
            else -> {
                if (selectedOption == correctAnswer) Color.Green
                else Color.Red
            }
        }
    } else if (correctAnswer == answerId && !isTimerRunning) Color.Green
    else Color.Unspecified
}