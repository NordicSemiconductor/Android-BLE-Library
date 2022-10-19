package no.nordicsemi.android.ble.example.game.server.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.quiz.repository.Answer
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.server.viewmodel.ServerViewModel
import no.nordicsemi.android.ble.example.game.timer.ShowTimer
import no.nordicsemi.android.common.navigation.NavigationManager

@Composable
fun QuestionsScreenServer(
    navigationManager: NavigationManager,
    question: Question
) {
    Column {
        Column {
            var enableNext by mutableStateOf(false)
            var selectedAnswerId by mutableStateOf(-1)
            val serverViewModel: ServerViewModel = hiltViewModel()
            val ticks by serverViewModel.ticks.collectAsState()

            ShowTimer( ticks, ticks.toFloat() / serverViewModel.ticksTotal )


            QuestionContentServer(
                question = question,
                selectedAnswer = selectedAnswerId,
                isTimerRunning = ticks > 0,
                onAnswer = {
                    selectedAnswerId = it.id
                    enableNext = true
                },
                modifier = Modifier
                    .fillMaxSize()
            )
        }

    }
}

@Composable
fun QuestionContentServer(
    question: Question,
    selectedAnswer: Int,
    isTimerRunning: Boolean,
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

            SingleChoiceQuestionServer(
                question = question,
                selectedAnswer = selectedAnswer,
                enableQuestionSelection = isTimerRunning,
                onAnswerSelected = { answer -> onAnswer(answer) },
                modifier = Modifier.fillParentMaxWidth()
            )
        }

    }
}

@Composable
fun SingleChoiceQuestionServer(
    question: Question,
    selectedAnswer: Int,
    enableQuestionSelection: Boolean,
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
                            question = question,
                            answerId = answer.id
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
    BottomBarClient(
        question = question,
        isTimerRunning = enableQuestionSelection,
        givenAnswer = selectedOption,
        onNextPressed = {
            if (!enableQuestionSelection) {
//                TODO : Implement the onClickNext event
                Log.d("AAA Server Screen", "QuestionsScreenServer: Show next Question")
            }
        }
    )
}

@Composable
fun checkColor(
    isTimerRunning: Boolean,
    selectedOption: Int,
    question: Question,
    optionSelected: Boolean,
    answerId: Int
): Color {
    return if (optionSelected) {
        when (isTimerRunning) {
            true -> MaterialTheme.colorScheme.secondary
            else -> {
                if (selectedOption == question.correctAnswerId) Color.Green
                else  Color.Red
            }
        }
    }
     else if (question.correctAnswerId == answerId && !isTimerRunning )  Color.Green
     else Color.Unspecified
}

@Composable
fun BottomBarClient(
    question: Question,
    isTimerRunning: Boolean,
    givenAnswer: Int,
    onNextPressed: () -> Unit,

    ) {

    if (!isTimerRunning) {

        // TODO onNextPressed and onDonePressed implementation is not completed yet
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {

            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                onClick = onNextPressed

            ) {
                Text(text = stringResource(id = R.string.next))
            }
        }
    }
}



