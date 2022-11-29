package no.nordicsemi.android.ble.example.game.server.repository

import no.nordicsemi.android.ble.example.game.server.api.QuestionsService
import no.nordicsemi.android.ble.example.game.server.model.QuestionMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val service: QuestionsService
) {
    suspend fun getQuestions(category: Int?): Questions {
        val questionsRemote = service.getQuestions(10, category)
        return QuestionMapper.mapRemoteToLocal(questionsRemote)
    }

}