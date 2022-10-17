package no.nordicsemi.android.ble.example.game.quiz.view

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShowAnswer(
    questionState: QuestionState
) {
    val context = LocalContext.current
//    TODO("Not yet implemented")
    if (questionState.givenAnswerId == questionState.question.correctAnswerId) {
        Toast.makeText(
            context,
            "The answer is correct: ${questionState.question.correctAnswerId}",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        Toast.makeText(
            context,
            "The answer is incorrect: ${questionState.question.correctAnswerId}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

