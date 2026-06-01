package com.marathon.tracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "marathon_prefs")

fun getDataStore(context: Context): DataStore<Preferences> =
    context.applicationContext.appDataStore
