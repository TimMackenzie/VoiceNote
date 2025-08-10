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

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.preference.PreferenceManager

class VoiceNoteActivity : Activity() {
    private var hasRun = false
    private var destination: String? = null
    private var enableExtendedTimeout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transparent)

        hasRun = false

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        destination = prefs.getString(
                getString(R.string.pref_key_destination),
                getString(R.string.pref_destination_value_keep)
        )
        enableExtendedTimeout = prefs.getBoolean(
                getString(R.string.pref_key_extended_timeout),
                false
        )

        if (DEBUG_ENABLE) {
            Log.i("Destination", destination ?: "null")
        }
    }

    // Wait until window shows the first time before starting voice recognition
    // This solves cancellation when starting from lock screen; only run once per entry to focus
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasRun) {
            hasRun = true
            startVoiceRecognitionActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VOICE_NOTE_RETURN && resultCode == PARAM_2_NOT_USED) {
            val results: ArrayList<String> =
            data?.getStringArrayListExtra(ANDROID_SPEECH_EXTRA_RESULTS) ?: arrayListOf()

            var spoken = ""
            if (results.isNotEmpty()) {
                spoken += results[0]
            }

            if (!handleKeywords(spoken)) {
                Toast.makeText(this, spoken, Toast.LENGTH_SHORT).show()

                when {
                    FORCE_COLORNOTE -> sendTextToTargetPackage(spoken, PACKAGE_NAME_COLORNOTE)
                    FORCE_KEEP -> sendTextToTargetPackage(spoken, PACKAGE_NAME_KEEP)
                    FORCE_EVERNOTE -> sendTextToTargetPackage(spoken, PACKAGE_NAME_EVERNOTE)
                    destination == getString(R.string.pref_destination_value_chooser) -> sendToChooser(spoken)
                    else -> sendText(spoken)
                }
            }
        }
        finish()
    }

    // Respond to a few special keywords instead of passing the text on
    private fun handleKeywords(text: String): Boolean {
        var foundMatch = true // set to false in default case below
        when {
            KEYWORD_PREFERENCES.equals(text, ignoreCase = true) -> {
                Toast.makeText(this, getString(R.string.keyword_preferences), Toast.LENGTH_SHORT).show()
                launchSettings()
            }
            KEYWORD_SETTINGS.equals(text, ignoreCase = true) -> {
                Toast.makeText(this, getString(R.string.keyword_settings), Toast.LENGTH_SHORT).show()
                launchSettings()
            }
            KEYWORD_HELP.equals(text, ignoreCase = true) -> {
                Toast.makeText(this, getString(R.string.keyword_help), Toast.LENGTH_SHORT).show()
                launchHelp()
            }
            KEYWORD_ABOUT.equals(text, ignoreCase = true) -> launchAbout()
            KEYWORD_UPGRADE.equals(text, ignoreCase = true) ->
            Toast.makeText(this, getString(R.string.upgrade_is_compulsory), Toast.LENGTH_LONG).show()
            else -> foundMatch = false
        }
        return foundMatch
    }

    private fun launchSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    }

    private fun launchHelp() {
        val intent = Intent(this, Help::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    }

    private fun launchAbout() {
        val intent = Intent(this, About::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
    }

    private fun sendText(text: String) {
        val target: String? = when (destination) {
            getString(R.string.pref_destination_value_keep) -> PACKAGE_NAME_KEEP
            getString(R.string.pref_destination_value_evernote) -> PACKAGE_NAME_EVERNOTE
            getString(R.string.pref_destination_value_colornote) -> PACKAGE_NAME_COLORNOTE
            getString(R.string.pref_destination_value_obsidian) -> PACKAGE_NAME_OBISDIAN
            else -> null // use null instead of pref_destination_value_default
        }

        Log.d("sendText", "target: $target")
        sendTextToTargetPackage(text, target)
    }

    /**
     * Force a chooser to be shown, rather than the automatic chooser which is only shown if no default is set
     */
    private fun sendToChooser(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIMETYPE_TEXT_PLAIN
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, null)
        try {
            startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.message_send_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTextToTargetPackage(text: String, target: String?) {
        try {
            if (DEBUG_ENABLE) {
                Log.i("sendTextToTargetPackage", target ?: "null")
            }

            val intent = Intent()
            if (target != null) {
                intent.`package` = target
            }
            intent.type = MIMETYPE_TEXT_PLAIN

            // Extra setup for Keep - pre-select account
            if (destination == getString(R.string.pref_destination_value_keep)) {
                intent.action = INTENT_ACTION_KEEP
                // setKeepExtra(intent)
            } else {
                intent.action = Intent.ACTION_SEND
            }

            if (!EMPTY_TITLE) {
                intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.message_title))
            }
            if (!EMPTY_SUBJECT) {
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.message_subject))
            }
            intent.putExtra(Intent.EXTRA_TEXT, text)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.message_send_failed), Toast.LENGTH_SHORT).show()
        } catch (_: NullPointerException) {
            Toast.makeText(this, getString(R.string.message_send_failed), Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * Add account information to Keep intent
     * TODO this is not working yet, so the preference has been hidden
     */
    private fun setKeepExtra(intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val currentAccount = prefs.getString(getString(R.string.pref_key_keep_account), null)

        if (DEBUG_ENABLE) {
            Log.i("Account for Keep", currentAccount ?: "null")
        }

        intent.putExtra(INTENT_EXTRA_AUTH_ACCOUNT, currentAccount)
        intent.putExtra(INTENT_EXTRA_ACCOUNT_TYPE, INTENT_EXTRA_ACCOUNT_TYPE_VALUE)
    }

    fun startVoiceRecognitionActivity() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // If recognizing in a different language than system default, set EXTRA_LANGUAGE
            // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, resources.getString(R.string.speech_prompt))

            if (enableExtendedTimeout) {
                intent.putExtra(
                        RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                        COMPLETION_TIMEOUT
                )
            }
            startActivityForResult(intent, VOICE_NOTE_RETURN)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.speech_fail), Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = getString(R.string.voicesearch_uri).toUri()
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
            try {
                startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                Log.e("startVoiceRecognition", getString(R.string.voicesearch_install_failure))
            }
            finish()
        }
    }

    companion object {
        const val VOICE_NOTE_RETURN = 853
        const val COMPLETION_TIMEOUT = 3000L

        const val KEYWORD_PREFERENCES = "preferences"
        const val KEYWORD_SETTINGS = "settings"
        const val KEYWORD_HELP = "help"
        const val KEYWORD_ABOUT = "transparent"
        const val KEYWORD_UPGRADE = "upgrade"

        // Skip the title and subject if set here
        const val EMPTY_TITLE = true
        const val EMPTY_SUBJECT = true

        // Test flags to hard-code behavior
        const val FORCE_KEEP = false
        const val FORCE_COLORNOTE = false
        const val FORCE_EVERNOTE = false

        const val DEBUG_ENABLE = false

        // Package names; receiving app decides what to do with data
        const val PACKAGE_NAME_KEEP = "com.google.android.keep"
        const val PACKAGE_NAME_COLORNOTE = "com.socialnmobile.dictapps.notepad.color.note"
        const val PACKAGE_NAME_EVERNOTE = "com.evernote"
        const val PACKAGE_NAME_OBISDIAN = "md.obsidian"

        const val INTENT_ACTION_KEEP = "com.google.android.gms.actions.CREATE_NOTE"

        const val MIMETYPE_TEXT_PLAIN = "text/plain"
        const val ANDROID_SPEECH_EXTRA_RESULTS = "android.speech.extra.RESULTS"
        const val PARAM_2_NOT_USED = -1 // keep original behavior (RESULT_OK)

        const val INTENT_EXTRA_AUTH_ACCOUNT = "authAccount"
        const val INTENT_EXTRA_ACCOUNT_TYPE = "accountType"
        const val INTENT_EXTRA_ACCOUNT_TYPE_VALUE = "com.google"
    }
}