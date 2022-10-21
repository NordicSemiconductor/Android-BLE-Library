package no.nordicsemi.android.ble.example.game.server.view

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.ble.example.game.R
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.view.QuestionContent

@Composable
fun QuestionsScreenServer(
    question: Question,
    selectedAnswerId: Int?,
    correctAnswerId: Int?,
    ticks: Long,
    modifier: Modifier = Modifier,
    onNextPressed: () -> Unit,
    onAnswerSelected: (Int) -> Unit,
) {
    QuestionContent(
        question = question,
        selectedAnswerId = selectedAnswerId,
        correctAnswerId = correctAnswerId,
        onAnswerSelected = onAnswerSelected,
        ticks = ticks,
        modifier = modifier
    )
    Button(
        modifier = modifier,
        onClick =  onNextPressed,
        enabled = correctAnswerId != null

    ) {
        Text(text = stringResource(id = R.string.next))
    }

}
