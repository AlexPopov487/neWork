package com.example.netologydiploma.data

import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.AppDb
import com.example.netologydiploma.db.EventDao
import com.example.netologydiploma.db.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun providePostRepository(
        apiService: ApiService,
        postDao: PostDao,
        appDb: AppDb
    ): PostRepository =
        PostRepository(postDao, apiService, appDb)

    @Provides
    @Singleton
    fun providesSignInUpRepository(apiService: ApiService): SignInUpRepository =
        SignInUpRepository(apiService)

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService,
        eventDao: EventDao,
        appDb: AppDb
    ): EventRepository = EventRepository(appDb, apiService, eventDao)
}