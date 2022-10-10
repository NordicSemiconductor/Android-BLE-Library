package no.nordicsemi.android.ble.example.game.quiz.model

import no.nordicsemi.android.ble.example.game.quiz.repository.Answer
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.Questions
import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionsRemote

object QuestionMapper {
    fun mapRemoteToLocal(questionRemote: QuestionsRemote): Questions {
        val results = questionRemote.results.map { remoteQuestion ->
            val answers = mutableListOf<Answer>()
            answers.add(Answer(remoteQuestion.correct_answer))
            answers.addAll(remoteQuestion.incorrect_answers.map {
                Answer(it)
            })
            answers.shuffle()
            Question(remoteQuestion.question, answers, remoteQuestion.correct_answer)

        }

        return Questions(results)
    }
}