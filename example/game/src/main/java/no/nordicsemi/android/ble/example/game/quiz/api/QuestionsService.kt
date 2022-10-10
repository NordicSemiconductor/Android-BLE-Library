package no.nordicsemi.android.ble.example.game.quiz.api

import no.nordicsemi.android.ble.example.game.quiz.repository.QuestionsRemote
import retrofit2.http.GET
import retrofit2.http.Query

interface QuestionsService {

    @GET("api.php?difficulty=hard&type=multiple")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int?,
    ): QuestionsRemote

}