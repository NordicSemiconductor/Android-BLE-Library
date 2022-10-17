package no.nordicsemi.android.ble.example.game.quiz.model

import no.nordicsemi.android.ble.example.game.quiz.repository.Answer
import no.nordicsemi.android.ble.example.game.quiz.repository.Question
import no.nordicsemi.android.ble.example.game.quiz.repository.Questions
import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionsRemote
import kotlin.random.Random
/**
 * A mapper object to map the remote data with the Local data format.
 */

object QuestionMapper {
    fun mapRemoteToLocal(questionRemote: QuestionsRemote): Questions {
        val results = questionRemote.results.map { remoteQuestion ->
            val answers = mutableListOf<Answer>()
            val correctId = Random.nextInt()
            answers.add(Answer(remoteQuestion.correct_answer, correctId))
            answers.addAll(remoteQuestion.incorrect_answers.map {
                Answer(it, Random.nextInt())
            })
            answers.shuffle()
            Question(remoteQuestion.question, answers, correctId)
        }

        return Questions(results)
    }
}