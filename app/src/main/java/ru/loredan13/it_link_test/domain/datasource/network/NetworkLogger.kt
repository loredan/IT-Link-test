package ru.loredan13.it_link_test.domain.datasource.network

interface NetworkLogger {
    fun log(message: String)
}

class NoOpLogger : NetworkLogger {
    override fun log(message: String) {}
}