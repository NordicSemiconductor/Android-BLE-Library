package no.nordicsemi.android.ble.example.game.quiz.repository

import no.nordicsemi.android.ble.example.game.proto.AnswerProto
import no.nordicsemi.android.ble.example.game.proto.QuestionProto

// Remote Question data

data class QuestionsRemote(
    val response_code: Int,
    val results: List<QuestionRemote>
)

data class QuestionRemote(
    val category: String,
    val correct_answer: String,
    val difficulty: String,
    val incorrect_answers: List<String>,
    val question: String,
    val type: String
)


// --------
// Local data

data class Questions(
    val questions: List<Question>,
)

data class Question(
    val question: String,
    val answers: List<Answer>,
    val correctAnswerId: Int? = null,
)

data class Answer(
    val text: String,
    val id: Int,
)

fun Question.toProto() = QuestionProto(question, answers.map { it.toProto() })

fun Answer.toProto() = AnswerProto(text, id)

fun QuestionProto.toQuestion() = Question(text, answers.map { it.toAnswer() })

fun AnswerProto.toAnswer() = Answer(text, id)
