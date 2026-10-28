package ru.loredan13.it_link_test.domain.datasource

interface NetworkLogger {
    fun log(message: String)
}

class NoOpLogger : NetworkLogger {
    override fun log(message: String) {}
}