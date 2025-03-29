package com.chivorn.okhttp.logger.interceptor.utils

import java.io.PrintWriter
import java.io.StringWriter


object ErrorUtil {
    fun getStackTrace(exception: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw, true)
        exception.printStackTrace(pw)
        return sw.buffer.toString()
    }
}