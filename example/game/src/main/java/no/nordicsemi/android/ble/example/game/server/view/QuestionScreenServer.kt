package no.nordicsemi.android.ble.example.game.server.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.timer.ShowTimer
import no.nordicsemi.android.ble.example.game.timer.TimerViewModel
import no.nordicsemi.android.ble.example.game.quiz.view.*
import no.nordicsemi.android.common.navigation.NavigationManager

@Composable
fun QuestionsScreenServer(
    navigationManager: NavigationManager,
    questions: Questions,
    onDonePressed: () -> Unit
) {
    Column {
        Column {
            val questionState = remember(questions.currentQuestionIndex) {
                questions.questionsState[questions.currentQuestionIndex]
            }

            val timerViewModel: TimerViewModel = hiltViewModel()
            val ticks by timerViewModel.ticks.collectAsState()
            val isTimerRunning by timerViewModel.timerState.collectAsState(true)
            val progress by timerViewModel.progress.collectAsState(initial = 1f)

            ShowTimer( ticks, progress )
            LaunchedEffect(key1 = Unit ){
                timerViewModel.startCountDown()
            }

            QuestionContent(
                question = questionState.question,
                selectedAnswer = questionState.givenAnswerId,
                onAnswer = {
                    questionState.givenAnswerId = it.id
                    questionState.enableNext = true
                },
                modifier = Modifier
                    .fillMaxSize()
            )
            BottomBar(
                questionState = questionState,
                isTimerRunning = isTimerRunning,
                onNextPressed = {
                    if (!isTimerRunning){
                        questions.currentQuestionIndex++
                    }
                },
                onDonePressed = onDonePressed,
            )
        }

    }
}


@Composable
fun BottomBar(
    questionState: QuestionState,
    isTimerRunning: Boolean,
    onNextPressed: () -> Unit,
    onDonePressed: () -> Unit
) {

    Log.d("BottomBar", "BottomBar: $isTimerRunning ")
    if (!isTimerRunning) {
        Log.d("BottomBar", "BottomBar: isTimerRunning ")
        ShowAnswer(questionState)

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
                enabled = questionState.enableNext,
                onClick =
                if (questionState.showDone) onDonePressed
                else onNextPressed

            ) {
                if (questionState.showDone)
                    Text(text = stringResource(id = no.nordicsemi.android.ble.example.game.R.string.done))
                else
                    Text(text = stringResource(id = no.nordicsemi.android.ble.example.game.R.string.next))
            }
        }
    }
}
