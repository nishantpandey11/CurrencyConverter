package com.currency.converter.utils


class DefaultTimeProvider : TimeProvider {
    override fun currentTimeSeconds(): Long = System.currentTimeMillis()
}