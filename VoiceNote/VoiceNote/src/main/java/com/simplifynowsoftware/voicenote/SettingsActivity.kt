/*
 * Copyright (C) 2013-2025 Simplify Now, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.simplifynowsoftware.voicenote

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceManager

class SettingsActivity : PreferenceActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupSimplePreferencesScreen()
    }

    // Shows the simplified settings UI if the device configuration dictates that a simplified, single-pane UI should be shown.
    private fun setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) return

        // In the simplified UI, fragments are not used at all and we instead use
        // the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general)

        // Add 'data' preferences, and a corresponding header.
        val fakeHeader = PreferenceCategory(this)
        fakeHeader.setTitle(R.string.pref_header_data)
        preferenceScreen.addPreference(fakeHeader)
        addPreferencesFromResource(R.xml.pref_data)

        // Bind the summaries of relevant preferences to their values.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_destination)))
        // bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_keep_account)))
    }

    override fun onIsMultiPane(): Boolean = isLargeTablet(this) && !isSimplePreferences(this)

    companion object {
        // Determines whether to always show the simplified settings UI
        private const val ALWAYS_SIMPLE_PREFS: Boolean = false

        // Helper: determine if the device has an extra-large screen (e.g., 10" tablets)
        private fun isLargeTablet(context: Context): Boolean {
            return (context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        // Determines whether the simplified settings UI should be shown.
        private fun isSimplePreferences(context: Context): Boolean {
            return ALWAYS_SIMPLE_PREFS || !isLargeTablet(context)
        }

        // Listener that updates a preference's summary to reflect its new value.
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(stringValue)
                preference.summary = if (index >= 0) preference.entries[index] else null
            } else {
                preference.summary = stringValue
            }
            true
        }

        // Binds a preference's summary to its value and triggers immediately
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener
            val current = PreferenceManager.getDefaultSharedPreferences(preference.context)
                .getString(preference.key, "") ?: ""
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, current)
        }
    }

    override fun onBuildHeaders(target: MutableList<Header>) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target)
        }
    }

    /**
     * isValidFragment was added in API 19 to address a security vulnerability.
     * Ensure that the fragment is one of our known fragments.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return GeneralPreferenceFragment::class.java.name == fragmentName ||
            DataPreferenceFragment::class.java.name == fragmentName
    }

    /** This fragment shows general preferences only. */
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
        }
    }

    /** This fragment shows data and sync preferences only. */
    class DataPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data)

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_destination)))
            // bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_keep_account)))
        }
    }
}