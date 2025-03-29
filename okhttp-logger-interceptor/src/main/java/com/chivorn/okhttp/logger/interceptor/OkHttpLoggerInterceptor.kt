package com.chivorn.okhttp.logger.interceptor

import com.chivorn.okhttp.logger.interceptor.utils.LogUtil
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class OkHttpLoggerInterceptor : Interceptor {
    private val tagName = this::class.java.simpleName

    @set:JvmName("level")
    @Volatile
    var level = Level.NONE

    enum class Level {
        /** No logs. */
        NONE,
        BASIC,
        HEADERS,
        BODY
    }

    fun setLevel(level: Level) = apply {
        this.level = level
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var exceptionToLog: Exception? = null
        val builder: Request.Builder = chain.request().newBuilder()
        val request = builder.build()
        val level = this.level
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        // Todo: Handle log format base on each log level

        var okhttpResponse: Response? = null
        val startNs = System.nanoTime()
        try {
            okhttpResponse = chain.proceed(request)
            return okhttpResponse
        } catch (e: Exception) {
            exceptionToLog = e
            throw e
        } finally {
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            LogUtil.writeCustomLog(
                tagName = tagName,
                request = request,
                okhttpResponse = okhttpResponse,
                tookMsTime = tookMs,
                exception = exceptionToLog
            )
        }
    }
}