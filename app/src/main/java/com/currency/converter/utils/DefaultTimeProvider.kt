package com.currency.converter.utils

import java.util.concurrent.TimeUnit


class DefaultTimeProvider : TimeProvider {
    override fun currentTimeSeconds(): Long = /*TimeUnit.MILLISECONDS.toSeconds(*/System.currentTimeMillis()
}