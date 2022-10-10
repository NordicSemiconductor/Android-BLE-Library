package no.nordicsemi.android.ble.example.game.quiz.repository

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

data class Questions(
    val questions: List<Question>,
)

data class Question(
    val question: String,
    val answers: List<Answer>,
    val correctAnswerId: String,
)

data class Answer(
    val text: String,
)
