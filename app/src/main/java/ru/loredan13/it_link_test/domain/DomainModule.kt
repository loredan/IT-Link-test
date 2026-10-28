package ru.loredan13.it_link_test.domain

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.loredan13.it_link_test.domain.datasource.network.NetworkDataSource
import ru.loredan13.it_link_test.domain.datasource.network.NetworkDataSourceImpl
import ru.loredan13.it_link_test.domain.datasource.network.NetworkLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    @Singleton
    fun providesNetworkDataSource(networkLogger: NetworkLogger): NetworkDataSource =
        NetworkDataSourceImpl(networkLogger)
}