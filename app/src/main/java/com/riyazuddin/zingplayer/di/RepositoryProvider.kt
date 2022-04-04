package com.riyazuddin.zingplayer.di

import com.riyazuddin.zingplayer.repository.remote.FirebaseMusicDatabase
import com.riyazuddin.zingplayer.repository.remote.IFirebaseMusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvider {

    @Singleton
    @Provides
    fun provideFirebaseMusicDatabase() = FirebaseMusicDatabase() as IFirebaseMusicDatabase

}