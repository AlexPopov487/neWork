package com.example.netologydiploma.data

import com.example.netologydiploma.api.ApiService
import com.example.netologydiploma.db.*
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
        appDb: AppDb,
        postRemoteKeyDao: PostRemoteKeyDao
    ): PostRepository =
        PostRepository(postDao, apiService, appDb, postRemoteKeyDao)

    @Provides
    @Singleton
    fun providesSignInUpRepository(apiService: ApiService): SignInUpRepository =
        SignInUpRepository(apiService)

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService,
        eventDao: EventDao,
        appDb: AppDb,
        eventRemoteKeyDao: EventRemoteKeyDao
    ): EventRepository = EventRepository(appDb, apiService, eventDao, eventRemoteKeyDao)

    @Provides
    @Singleton
    fun provideProfileRepository(
        apiService: ApiService,
        appDb: AppDb,
        wallRemoteKeyDao: WallRemoteKeyDao,
        wallPostDao: WallPostDao,
        jobDao: JobDao,
        postDao: PostDao,
    ): ProfileRepository =
        ProfileRepository(apiService, appDb, wallPostDao, wallRemoteKeyDao, jobDao, postDao)


    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        userDao: UserDao,
    ): UserRepository =
        UserRepository(apiService, userDao)
}