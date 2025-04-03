package com.chivorn.okhttp.logger.interceptor.utils

import android.os.Build
import android.util.Log
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.IOException
import timber.log.Timber
import java.util.Collections

object LogUtil {
    private const val tagFrame = "===================="
    private const val titleFrame = "--------------------"
    private const val space = " "
    private const val MAX_LOG_BYTES = 2000
    private fun getTextWithFrame(text: String, frame: String): String {
        return frame + space + text + space + frame
    }

    private fun getPrettyHeader(headerText: String): String {
        return """
                       
               ${getTextWithFrame(headerText, tagFrame)}
               
               """.trimIndent()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPrettyHeader(header: String, param: LinkedHashMap<String, String>?): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(" ")
        stringBuilder.append(getPrettyHeader(header))
        val result: String?
        if (!param.isNullOrEmpty()) {
            val entrySet: Set<*> = param.entries
            val iterator: Iterator<Map.Entry<*, *>> =
                entrySet.iterator() as Iterator<Map.Entry<*, *>>
            var currentIndex = 0
            var compareMaxLength: Comparator<String>? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                compareMaxLength = Comparator.comparingInt { obj: String -> obj.length }
            }
            val longKey = Collections.max(param.keys, compareMaxLength)
            val maxKeyLength = longKey.length
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value != null) {
                    val key = StringBuilder(entry.key.toString())
                    val keyLength = key.length
                    if (keyLength < maxKeyLength) {
                        for (i in 0 until maxKeyLength - keyLength) {
                            key.append(space)
                        }
                    }
                    if (key.toString().isEmpty()) {
                        stringBuilder.append(entry.value.toString())
                    } else {
                        stringBuilder.append("# ").append(key).append(" : ")
                            .append(entry.value.toString())
                    }
                    if (currentIndex < param.size) {
                        stringBuilder.append("\r\n")
                    }
                }
                currentIndex++
            }
            stringBuilder.append(" ")
            result = stringBuilder.toString()
            return result
        }
        return ""
    }

    fun writeCustomLog(
        tagName: String,
        request: Request,
        okhttpResponse: Response?,
        tookMsTime: Long,
        exception: Exception?
    ) {
        try {
            val logParam: LinkedHashMap<String, String> = LinkedHashMap()
            val logHeader = "Request Details"
            val httpUrl: HttpUrl = request.url
            val method: String = request.method
            // val header: Headers = request.headers()
            val path: String = httpUrl.toUrl().path
            val query: String? = httpUrl.query
            val url: String = request.url.encodedPath
            val requestBody: RequestBody? = request.body
            val responseCode: Int? = okhttpResponse?.code
            val responseBody: ResponseBody? = OkHttpUtil.getResponseBody(okhttpResponse)

            // Read and parse RequestBody with Gson to avoid Unicode Escaping in String Logging
            val requestBodyStr = requestBody?.let {
                val buffer = Buffer()
                it.writeTo(buffer)
                val rawStr = buffer.readUtf8()
                StringUtil.parseWithGson(rawStr)
            } ?: ""

            // Read and parse ResponseBody with Gson to avoid Unicode Escaping in String Logging
            val responseBodyString = responseBody?.let {
                val rawStr = it.string()
                StringUtil.parseWithGson(rawStr)
            } ?: ""

            logParam["Endpoint ($method)"] = httpUrl.toString()
            if (path != url) logParam["Url"] = url
            if (query != null) logParam["Url"] = "$url?$query"
            if (requestBodyStr.isNotEmpty()) logParam["Request"] = requestBodyStr
            if (responseCode != null) logParam["Response Code"] = responseCode.toString()
            logParam["Duration"] = "${tookMsTime}ms"
            if (responseBodyString.isNotEmpty()) logParam["Response"] = responseBodyString
            if (exception != null && exception !is IOException) logParam["Exception"] =
                ErrorUtil.getStackTrace(exception)

            val logMessage = getPrettyHeader(logHeader, logParam)
            if (responseCode != null && responseCode == 200) {
                logTextInChunks(tagName, logMessage, Log.DEBUG)
            } else {
                logTextInChunks(tagName, logMessage, Log.ERROR)
            }
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