package ru.loredan13.it_link_test.presentation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.loredan13.it_link_test.domain.datasource.DiskCacheDataSource
import ru.loredan13.it_link_test.domain.datasource.DiskCacheDataSourceImpl
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    @Provides
    @Singleton
    fun provideDiskCacheDataSource(@ApplicationContext context: Context): DiskCacheDataSource =
        DiskCacheDataSourceImpl(context.cacheDir.resolve("images.txt"))

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope