package com.simplifynowsoftware.voicenote

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_data, rootKey)
        addPreferencesFromResource(R.xml.pref_general)

        findPreference<ListPreference>(getString(R.string.pref_key_destination))?.apply {
            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }

        // for later use when keep-account is enabled:
        // findPreference<EditTextPreference>(getString(R.string.pref_key_keep_account))?.apply {
        //     summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        // }
    }
}