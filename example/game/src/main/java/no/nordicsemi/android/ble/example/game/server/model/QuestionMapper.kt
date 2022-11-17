package no.nordicsemi.android.ble.example.game.server.model

import no.nordicsemi.android.ble.example.game.server.repository.Answer
import no.nordicsemi.android.ble.example.game.server.repository.Question
import no.nordicsemi.android.ble.example.game.server.repository.Questions
import no.nordicsemi.android.ble.example.game.server.repository.QuestionsRemote

/**
 * A mapper object to map the remote data with the Local data format.
 */
object QuestionMapper {
    fun mapRemoteToLocal(questionRemote: QuestionsRemote): Questions {
        val results = questionRemote.results.map { remoteQuestion ->
            val ids = (0..remoteQuestion.incorrect_answers.size + 1).toList().shuffled()
            val answers = mutableListOf<Answer>()
            val correctId = ids[0]
            answers.add(Answer(remoteQuestion.correct_answer, correctId))
            answers.addAll(remoteQuestion.incorrect_answers.mapIndexed { index, answer ->
                Answer(answer, ids[index + 1])
            })
            answers.shuffle()
            Question(remoteQuestion.question, answers, correctId)
        }

        return Questions(results)
    }
}