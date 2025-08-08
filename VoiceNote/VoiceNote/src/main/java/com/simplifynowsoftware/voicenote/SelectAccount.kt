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

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log

class SelectAccount : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transparent)

        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(baseContext)

        val currentAccount = prefs.getString(getString(R.string.pref_key_keep_account), null)

        val account: Account? = currentAccount?.let {
            Account(it, ACCOUNT_TYPE_GOOGLE)
        }

        // Ask user to select which Google account to use
        @Suppress("DEPRECATION")
        val intent = AccountManager.newChooseAccountIntent(
            account,            // show existing selection, if any
            null,               // allowableAccounts
            arrayOf(ACCOUNT_TYPE_GOOGLE),
            false,              // alwaysPromptForAccount
            null, null, null, null
        )
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)

                if (VoiceNoteActivity.DEBUG_ENABLE) {
                    Log.i("accountName", accountName ?: "null")
                }

                val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
                prefs.edit()
                    .putString(getString(R.string.pref_key_keep_account), accountName)
                    .apply()
            }
            // Exit either way
            finish()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val REQUEST_CODE = 0x01F3
        const val ACCOUNT_TYPE_GOOGLE = "com.google"
    }
}