package ru.loredan13.it_link_test.presentation.di

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import ru.loredan13.it_link_test.domain.datasource.network.NetworkLogger

class DebugAwareLogger(context: Context) : NetworkLogger {
    private val isDebuggable =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    override fun log(message: String) {
        if (isDebuggable) {
            Log.d("Network", message)
        }
    }
}