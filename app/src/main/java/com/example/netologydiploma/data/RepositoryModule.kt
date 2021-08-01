package com.example.netologydiploma.data

import com.example.netologydiploma.api.ApiService
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
    fun providePostRepository(apiService: ApiService, postDao: PostDao) : PostRepository =
        PostRepository(postDao, apiService)

    @Provides
    @Singleton
    fun providesSignInUpRepository(apiService: ApiService) : SignInUpRepository =
        SignInUpRepository(apiService)

}