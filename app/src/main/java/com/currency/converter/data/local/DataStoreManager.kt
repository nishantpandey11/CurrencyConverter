package com.currency.converter.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.currency.converter.utils.TimeProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * A class for managing application preferences using DataStore.
 *
 * @property dataStore The [DataStore] instance used to manage application preferences.
 * @property timeProvider A [TimeProvider] for obtaining the current time.
 */
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val timeProvider: TimeProvider,
) {

    /**
     * Sets the first launch preference.
     *
     * @param value The boolean value indicating if it is the first launch.
     */
    suspend fun setFirstLaunch(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = value
        }
    }

    /**
     * Checks if it is the first launch of the application.
     *
     * @return `true` if it is the first launch, `false` otherwise.
     */
    suspend fun isFirstLaunch(): Boolean {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.FIRST_LAUNCH] ?: true
    }

    /**
     * Sets the timestamp of the last update in seconds.
     *
     * @param value The timestamp value in seconds.
     */
    suspend fun setTimestampInSeconds(value: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TIMESTAMP] = value
        }
    }

    /**
     * Retrieves the timestamp of the last update in seconds.
     *
     * @return The timestamp value in seconds, or [Constants.NO_DATA] if not set.
     */
    suspend fun getTimestampInSeconds(): Long {
        val preferences = dataStore.data.first()
        return preferences[PreferencesKeys.TIMESTAMP]?.toLong() ?: Constants.NO_DATA
    }

    /**
     * Checks if there is no data stored.
     *
     * @return `true` if there is no data, `false` otherwise.
     */
    suspend fun isDataEmpty(): Boolean {
        return getTimeSinceLastUpdateInSeconds() == Constants.NO_DATA
    }

    /**
     * Checks if the stored data is stale (older than 30 min ).
     *
     * @return `true` if the data is stale, `false` otherwise.
     */
    suspend fun isDataStale(): Boolean {
        return getTimeSinceLastUpdateInSeconds() > Constants.THIRTY_MIN_IN_SECONDS
    }

    /**
     * Retrieves the time since the last update in seconds.
     *
     * @return The time in seconds since the last update, or [Constants.NO_DATA] if no update has occurred.
     */
    private suspend fun getTimeSinceLastUpdateInSeconds(): Long {
        return if (getTimestampInSeconds() != Constants.NO_DATA) {
            timeProvider.currentTimeSeconds() - getTimestampInSeconds()
        } else {
            Constants.NO_DATA
        }
    }

    object PreferencesKeys {
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val TIMESTAMP = longPreferencesKey("timestamp")
    }

    object Constants {
        const val THIRTY_MIN_IN_SECONDS = 1800000L//60000L//
        const val NO_DATA = 0L
    }
}
