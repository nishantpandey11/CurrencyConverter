package com.currency.converter.utils

interface TimeProvider {
    fun currentTimeSeconds(): Long
}