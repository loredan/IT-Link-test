package ru.loredan13.it_link_test.presentation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.loredan13.it_link_test.domain.datasource.DiskCacheDataSource
import ru.loredan13.it_link_test.domain.datasource.DiskCacheDataSourceImpl
import ru.loredan13.it_link_test.domain.datasource.network.NetworkLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    @Provides
    @Singleton
    fun provideNetworkLogger(@ApplicationContext context: Context): NetworkLogger =
        DebugAwareLogger(context)

    @Provides
    @Singleton
    fun provideDiskCacheDataSource(@ApplicationContext context: Context): DiskCacheDataSource =
        DiskCacheDataSourceImpl(context.cacheDir.resolve("images.txt"))
}