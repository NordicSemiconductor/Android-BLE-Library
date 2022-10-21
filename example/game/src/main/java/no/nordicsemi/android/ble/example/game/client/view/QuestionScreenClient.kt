package no.nordicsemi.android.ble.example.game.client.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionContent

@Composable
fun QuestionScreenClient(
    question: Question,
    selectedAnswerId: Int?,
    correctAnswerId: Int?,
    ticks: Long,
    modifier: Modifier = Modifier,
    onAnswerSelected: (Int) -> Unit,
) {


    QuestionContent(
        question = question,
        selectedAnswerId = selectedAnswerId,
        correctAnswerId = correctAnswerId,
        onAnswerSelected = onAnswerSelected,
        ticks = ticks,
        modifier = modifier,
    )
}



