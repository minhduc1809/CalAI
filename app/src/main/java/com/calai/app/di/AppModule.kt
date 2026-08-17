package com.calai.app.di

import android.content.Context
import androidx.room.Room
import com.calai.app.data.local.CalAIDao
import com.calai.app.data.local.CalAIDatabase
import com.calai.app.data.remote.CalAIApi
import com.calai.app.data.repository.CalAIRepositoryImpl
import com.calai.app.domain.repository.CalAIRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun provideApi(): CalAIApi {
        return Retrofit.Builder()
            .baseUrl(CalAIApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CalAIApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(dao: CalAIDao): CalAIRepository {
        return CalAIRepositoryImpl(dao)
    }
}
