package com.chivorn.okhttp.logger.interceptor.utils

import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber

object OkHttpUtil {
    fun getResponseBody(response: Response?): ResponseBody? {
        return response?.peekBody(Long.MAX_VALUE)
    }

    fun okHttpResponseToJSONObject(response: Response?): JSONObject? {
        try {
            val responseBody: ResponseBody? = getResponseBody(response)
            responseBody?.string()?.let {
                return JSONObject(it)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }
}