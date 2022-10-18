package no.nordicsemi.android.ble.example.game.client.view

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
    val isTimerRunning = ticks>0

    ShowTimer(ticks, ticks.toFloat() / timerViewModel.ticksTotal )
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
    onAnswerSelected: (Answer) -> Unit,
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

            val materialPrimary = MaterialTheme.colorScheme.secondary
            val answerBackgroundColor = when (optionSelected) {
                true -> materialPrimary
                false -> Color.Unspecified
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = optionSelected,
                        onClick = onClickHandle,
                        enabled = enableQuestionSelection
                    )
                    .background(color = answerBackgroundColor, shape = RectangleShape)
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
 if (!enableQuestionSelection){
     ShowAnswerClient(correctAnswer = correctAnswer, selectedAnswerId = selectedOption)
 }
}
@Composable
fun ShowAnswerClient(correctAnswer: Int?, selectedAnswerId: Int) {
// TODO: Remove Toast messages and implement the actual code
    val context = LocalContext.current
    if (correctAnswer == selectedAnswerId){
        Toast.makeText(
            context,
            "The answer is correct: $selectedAnswerId",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        Toast.makeText(
            context,
            "The answer is correct: $selectedAnswerId",
            Toast.LENGTH_SHORT
        ).show()
    }
}

