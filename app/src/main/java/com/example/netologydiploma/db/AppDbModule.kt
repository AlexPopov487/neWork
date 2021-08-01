package com.example.netologydiploma.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module()
class AppDbModule {

    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): AppDb =
        Room.databaseBuilder(
            context,
            AppDb::class.java,
            "app.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePostDao(appDb: AppDb): PostDao = appDb.postDao()
}