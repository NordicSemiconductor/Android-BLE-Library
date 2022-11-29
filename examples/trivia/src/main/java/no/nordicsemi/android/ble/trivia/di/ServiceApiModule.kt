package no.nordicsemi.android.ble.trivia.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.ble.trivia.BuildConfig
import no.nordicsemi.android.ble.trivia.server.api.QuestionsService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceApiModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = when {
                BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
                else -> HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .followRedirects(false)
            .pingInterval(25, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory()).build()
    }

    @Provides
    @Singleton
    fun provideService(
        client: OkHttpClient,
        moshi: Moshi,
    ): QuestionsService {
        return Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build().create()
    }
}