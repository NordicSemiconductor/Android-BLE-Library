package no.nordicsemi.android.ble.example.game.quiz.repository

import no.nordicsemi.android.ble.example.game.proto.AnswerProto
import no.nordicsemi.android.ble.example.game.proto.QuestionProto

/**
 * Remote Question data fetched from a remote api.
 * @property response_code Response code (always 0).
 * @property results List of questions from the remote api.
 * */
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

/**
 * Local Question Data.
 * @property questions List of questions with list of answers and correct answer id.
 * */
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