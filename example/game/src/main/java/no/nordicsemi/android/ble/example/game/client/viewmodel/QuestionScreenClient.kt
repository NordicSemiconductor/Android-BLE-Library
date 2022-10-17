package no.nordicsemi.android.ble.example.game.client.viewmodel

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionContent
import no.nordicsemi.android.ble.example.game.timer.ShowTimer
import no.nordicsemi.android.ble.example.game.timer.TimerViewModel
import no.nordicsemi.android.common.navigation.NavigationManager


@Composable
fun QuestionScreenClient(
    navigationManager: NavigationManager,
    question: Question,
    Answer: Int?
) {
    var selectedAnswerId by mutableStateOf(0)
    val timerViewModel: TimerViewModel = hiltViewModel()
    val ticks by timerViewModel.ticks.collectAsState()
    val isTimerRunning by timerViewModel.timerState.collectAsState(false)
    val progress by timerViewModel.progress.collectAsState(initial = 1f)

    ShowTimer(ticks, progress)
    LaunchedEffect(key1 = Unit) {
        timerViewModel.startCountDown()
    }

    QuestionContent(
        question = question,
        selectedAnswer = selectedAnswerId,
        onAnswer = { selectedAnswerId = it.id },
        modifier = Modifier
            .fillMaxWidth()
    )
    BottomBarClient(
        selectedAnswer = selectedAnswerId,
        isTimerRunning = isTimerRunning,
        onNextPressed = { selectedAnswerId = it }
    )
}

@Composable
fun BottomBarClient(
    selectedAnswer: Int,
    isTimerRunning: Boolean,
    onNextPressed: (Int) -> Unit

) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {

        Button(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            onClick = {
//                TODO: onNextPressed is not completed yet
                onNextPressed(selectedAnswer)
                Log.d("AAAButton", "BottomBarClient: $selectedAnswer")
            }

        ) {
            Text(text = stringResource(id = R.string.next))
        }
    }
}
