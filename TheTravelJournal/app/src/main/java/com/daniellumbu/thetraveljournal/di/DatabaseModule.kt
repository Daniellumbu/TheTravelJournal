package com.daniellumbu.thetraveljournal.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.daniellumbu.thetraveljournal.data.AppDatabase
import com.daniellumbu.thetraveljournal.data.TodoDAO
import com.daniellumbu.thetraveljournal.data.MarkerDAO
import com.daniellumbu.thetraveljournal.data.MarkerDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun provideTodoAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return AppDatabase.getDatabase(appContext)
    }

    @Provides
    fun provideTodoDao(appDatabase: AppDatabase): TodoDAO {
        return appDatabase.todoDao()
    }

    @Provides
    @Singleton
    fun provideMarkerDatabase(@ApplicationContext appContext: Context): MarkerDatabase {
        return Room.databaseBuilder(
            appContext,
            MarkerDatabase::class.java,
            "marker_database"
        ).build()
    }

    @Provides
    fun provideMarkerDao(markerDatabase: MarkerDatabase): MarkerDAO {
        return markerDatabase.markerDao()
    }
}
