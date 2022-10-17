package no.nordicsemi.android.ble.example.game.quiz.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.ble.example.game.quiz.repository.Answer
import no.nordicsemi.android.ble.example.game.quiz.repository.Question


@Composable
fun QuestionTitle(
    questionTitle: String = "Question title not available"
) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = questionTitle,
            modifier = Modifier
                .fillMaxWidth(),
            fontWeight = Bold
        )
    }

}


@Composable
fun QuestionContent(
    question: Question,
    selectedAnswer: Int,
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
            QuestionTitle(question.question)
            Spacer(modifier = Modifier.height(24.dp))

            SingleChoiceQuestion(
                question = question,
                selectedAnswer = selectedAnswer,
                onAnswerSelected = { answer -> onAnswer(answer) },
                modifier = Modifier.fillParentMaxWidth()
            )
        }

    }
}

@Composable
fun SingleChoiceQuestion(
    question: Question,
    selectedAnswer: Int,
    onAnswerSelected: (Answer) -> Unit,
    modifier: Modifier = Modifier
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
//                        enabled = enableQuestionSelection
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
}




