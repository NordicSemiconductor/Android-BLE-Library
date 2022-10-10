package no.nordicsemi.android.ble.example.game.quiz.repository

import no.nordicsemi.android.ble.example.game.quiz.api.QuestionsService
import no.nordicsemi.android.ble.example.game.quiz.model.QuestionMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val service: QuestionsService
) {
    suspend fun getQuestions(category: Int?): Questions{
        val questionsRemote = service.getQuestions(10, category)
        return QuestionMapper.mapRemoteToLocal(questionsRemote)
    }

}