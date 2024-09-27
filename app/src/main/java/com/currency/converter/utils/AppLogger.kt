package com.currency.converter.utils

import com.currency.converter.BuildConfig

object AppLogger {

    private const val TAG = "AppLogger"

    fun e(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            println("$tag : $message")
        }
    }

}