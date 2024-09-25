package com.currency.converter.utils

import android.util.Log
import com.currency.converter.BuildConfig

object AppLogger {

    private const val TAG = "AppLogger"

    fun i(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }


    fun d(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message)
        }
    }

    fun w(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }

    fun v(tag: String = TAG, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message)
        }
    }
}