package com.chivorn.okhttp.logger.interceptor.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException

object StringUtil {
    internal fun parseWithGson(input: String): String {
        return if (input.isNotEmpty()) {
            try {
                val gson = Gson()
                val jsonElement = gson.fromJson(input, JsonElement::class.java)
                jsonElement.toString()
            } catch (e: JsonSyntaxException) {
                input
            }
        } else {
            input
        }
    }
}