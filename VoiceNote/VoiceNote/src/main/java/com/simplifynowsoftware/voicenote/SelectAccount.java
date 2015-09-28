/*
 * Copyright (C) 2013-2015 Simplify Now, LLC
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

package com.simplifynowsoftware.voicenote;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class SelectAccount extends Activity {
    protected final static int REQUEST_CODE = 0x01F3;
    protected final static String ACCOUNT_TYPE_GOOGLE = "com.google";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        final String currentAccount = prefs.getString(getString(R.string.pref_key_keep_account), null);

        Account account = null;
        if(null != currentAccount) {
             account = new Account(currentAccount, ACCOUNT_TYPE_GOOGLE);
        }

        // Ask user to select which Google account to use
        Intent intent = AccountManager.newChooseAccountIntent(
                account, // show existing selection, if any
                null,
                new String[]{ACCOUNT_TYPE_GOOGLE},
                false,
                null,
                null,
                null,
                null);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == REQUEST_CODE ) {
            if(resultCode == RESULT_OK) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                if (VoiceNoteActivity.DEBUG_ENABLE) {
                    Log.i("accountName", accountName);
                }

                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                prefs.edit().putString(getString(R.string.pref_key_keep_account), accountName).commit();
            }

            // Exit either way
            finish();
        }
    }
}
