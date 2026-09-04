package com.calai.app.di

import android.content.Context
import androidx.room.Room
import com.calai.app.data.local.CalAIDao
import com.calai.app.data.local.CalAIDatabase
import com.calai.app.data.local.TokenManager
import com.calai.app.data.remote.AuthInterceptor
import com.calai.app.data.remote.CalAIApi
import com.calai.app.data.repository.CalAIRepositoryImpl
import com.calai.app.domain.repository.CalAIRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CalAIDatabase {
        return Room.databaseBuilder(
            context,
            CalAIDatabase::class.java,
            "calai_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDao(db: CalAIDatabase): CalAIDao = db.dao

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CalAIApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): CalAIApi {
        return retrofit.create(CalAIApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(
        dao: CalAIDao,
        api: CalAIApi,
        tokenManager: TokenManager
    ): CalAIRepository {
        return CalAIRepositoryImpl(dao, api, tokenManager)
    }
}
