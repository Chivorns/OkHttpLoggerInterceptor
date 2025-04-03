package com.chivorn.okhttp.logger.interceptor.utils

import android.os.Build
import android.util.Log
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.IOException
import timber.log.Timber

object LogUtil {
    private const val TAG_FRAME = "===================="
    private const val TITLE_FRAME = "--------------------"
    private const val SPACE = " "
    private const val MAX_LOG_BYTES = 2000

    private fun getTextWithFrame(text: String, frame: String): String {
        return "$frame$SPACE$text$SPACE$frame"
    }

    private fun getPrettyHeader(headerText: String): String {
        return "\n${getTextWithFrame(headerText, TAG_FRAME)}\n"
    }

    private fun getPrettyHeader(header: String, param: LinkedHashMap<String, String>?): String {
        val stringBuilder = StringBuilder(getPrettyHeader(header))
        if (param.isNullOrEmpty()) return stringBuilder.toString()

        val longestKeyLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            param.keys.maxOfOrNull { it.length } ?: 0
        } else {
            param.keys.maxByOrNull { it.length }?.length ?: 0
        }

        param.entries.forEachIndexed { index, (key, value) ->
            if (value != null) {
                val paddedKey = key.padEnd(longestKeyLength, ' ')
                stringBuilder.append("# $paddedKey : $value")
                if (index < param.size - 1) stringBuilder.append("\r\n")
            }
        }
        stringBuilder.append("\n")
        return stringBuilder.toString()
    }

    fun writeCustomLog(
        tagName: String,
        request: Request,
        okhttpResponse: Response?,
        tookMsTime: Long,
        exception: Exception?
    ) {
        try {
            val logParam = LinkedHashMap<String, String>()
            val logHeader = "Request Details"
            val httpUrl = request.url
            val method = request.method
            val path = httpUrl.toUrl().path
            val query = httpUrl.query
            val url = request.url.encodedPath
            val requestBody = request.body
            val responseCode = okhttpResponse?.code
            val responseBody = OkHttpUtil.getResponseBody(okhttpResponse)

            // Read and parse RequestBody with Gson to avoid Unicode Escaping in String Logging
            val requestBodyStr = requestBody?.let {
                val buffer = Buffer()
                it.writeTo(buffer)
                StringUtil.parseWithGson(buffer.readUtf8())
            } ?: ""

            // Read and parse ResponseBody with Gson to avoid Unicode Escaping in String Logging
            val responseBodyString = responseBody?.let {
                StringUtil.parseWithGson(it.string())
            } ?: ""

            logParam["Endpoint ($method)"] = httpUrl.toString()
            if (path != url) logParam["Url"] = url
            if (query != null) logParam["Url"] = "$url?$query"
            if (requestBodyStr.isNotEmpty()) logParam["Request"] = requestBodyStr
            if (responseCode != null) logParam["Response Code"] = responseCode.toString()
            logParam["Duration"] = "${tookMsTime}ms"
            if (responseBodyString.isNotEmpty()) logParam["Response"] = responseBodyString
            if (exception != null && exception !is IOException) {
                logParam["Exception"] = exception.stackTraceToString()
            }

            val logMessage = getPrettyHeader(logHeader, logParam)
            val logLevel = if (responseCode == 200) Log.DEBUG else Log.ERROR
            logTextInChunks(tagName, logMessage, logLevel)
        } catch (e: Exception) {
            Timber.tag(tagName).e(e)
        }
    }

    private fun logTextInChunks(tag: String, content: String, logLevel: Int = Log.DEBUG) {
        var remainingContent = content
        while (remainingContent.isNotEmpty()) {
            val chunk = remainingContent.take(MAX_LOG_BYTES)
            remainingContent = remainingContent.drop(chunk.length)
            when (logLevel) {
                Log.ERROR -> Timber.tag(tag).e(chunk)
                Log.WARN -> Timber.tag(tag).w(chunk)
                Log.INFO -> Timber.tag(tag).i(chunk)
                Log.DEBUG -> Timber.tag(tag).d(chunk)
                else -> Timber.tag(tag).v(chunk)
            }
        }
    }
}