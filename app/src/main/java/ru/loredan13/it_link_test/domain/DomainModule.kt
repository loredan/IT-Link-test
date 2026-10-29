package ru.loredan13.it_link_test.domain

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.loredan13.it_link_test.domain.datasource.NetworkDataSource
import ru.loredan13.it_link_test.domain.datasource.NetworkDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Binds
    @Singleton
    abstract fun bindNetworkDataSource(impl: NetworkDataSourceImpl): NetworkDataSource
}