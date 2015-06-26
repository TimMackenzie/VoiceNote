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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class VoiceNoteActivity extends Activity {
    protected static final int VOICE_NOTE_RETURN            = 853;
    protected static final long COMPLETION_TIMEOUT          = 3000;

    protected static final String KEYWORD_PREFERENCES       = "preferences";
    protected static final String KEYWORD_SETTINGS          = "settings";
    protected static final String KEYWORD_HELP              = "help";
    protected static final String KEYWORD_ABOUT             = "transparent";
    protected static final String KEYWORD_UPGRADE           = "upgrade";

    // Skip the title and subject if set here
    protected static final boolean EMPTY_TITLE              = true;
    protected static final boolean EMPTY_SUBJECT            = true;

    // Test flags to hard-code behavior
    protected static final boolean FORCE_KEEP               = false;
    protected static final boolean FORCE_COLORNOTE          = false;
    protected static final boolean FORCE_EVERNOTE           = false;

    protected static final boolean DEBUG_ENABLE             = false;

    // Package names; receiving app decides what to do with data
    protected static final String PACKAGE_NAME_KEEP         = "com.google.android.keep";
    protected static final String PACKAGE_NAME_COLORNOTE    = "com.socialnmobile.dictapps.notepad.color.note";
    protected static final String PACKAGE_NAME_EVERNOTE     = "com.evernote";

    protected static final String MIMETYPE_TEXT_PLAIN           = "text/plain";
    protected static final String ANDROID_SPEECH_EXTRA_RESULTS  = "android.speech.extra.RESULTS";
    protected static final int PARAM_2_NOT_USED                 = -1;

    protected boolean mHasRun;
    protected String mDestination;
    protected boolean mEnableExtendedTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent);

        mHasRun = false;

        // Load preferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        mDestination = prefs.getString(
                getString(R.string.pref_key_destination),
                getString(R.string.pref_destination_value_keep));

        mEnableExtendedTimeout = prefs.getBoolean(
                getString(R.string.pref_key_extended_timeout),
                false);

        if(DEBUG_ENABLE) {
            Log.i("Destination", mDestination);
        }
    }

    /*
     * Wait until window shows the first time before starting voice recognition
     *  This solves problem of cancellation when starting from lock screen.
     * Also, ensure we only call on the first time activity comes into focus.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(!mHasRun) {
            mHasRun = true;
            startVoiceRecognitionActivity();
        }
    }

    @Override
    protected void onActivityResult(int paramInt1, int paramInt2, Intent paramIntent)
    {
        if ((paramInt1 == VOICE_NOTE_RETURN) && (paramInt2 == PARAM_2_NOT_USED)) {
            ArrayList arrayList = paramIntent.getStringArrayListExtra(ANDROID_SPEECH_EXTRA_RESULTS);
            String str = "";
            if (arrayList.size() > 0) {
                str = str + arrayList.get(0);
            }

            if(!handleKeywords(str)) {
                Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

                if(FORCE_COLORNOTE) {
                    sendTextToTargetPackage(str, PACKAGE_NAME_COLORNOTE);
                } else if (FORCE_KEEP) {
                    sendTextToTargetPackage(str, PACKAGE_NAME_KEEP);
                } else if (FORCE_EVERNOTE) {
                    sendTextToTargetPackage(str, PACKAGE_NAME_EVERNOTE);
                } else {
                    sendText(str);
                }
            }
        }

        finish();
    }


    // Respond to a few special keywords instead of passing the text on
    protected boolean handleKeywords(String text) {
        boolean foundMatch = true; // set to false in default case below

        // Special cases - handle keywords
        if(KEYWORD_PREFERENCES.equalsIgnoreCase(text)) {
            Toast.makeText(this, getString(R.string.keyword_preferences), Toast.LENGTH_SHORT).show();
            launchSettings();
        } else if(KEYWORD_SETTINGS.equalsIgnoreCase(text)) {
            Toast.makeText(this, getString(R.string.keyword_settings), Toast.LENGTH_SHORT).show();
            launchSettings();
        } else if(KEYWORD_HELP.equalsIgnoreCase(text)) {
            Toast.makeText(this, getString(R.string.keyword_help), Toast.LENGTH_SHORT).show();
            launchHelp();
        } else if(KEYWORD_ABOUT.equalsIgnoreCase(text)) {
            launchAbout();
        } else if(KEYWORD_UPGRADE.equalsIgnoreCase(text)) {
            Toast.makeText(this, getString(R.string.upgrade_is_compulsory), Toast.LENGTH_SHORT).show();
        } else {
            foundMatch = false;
        }

        return foundMatch;
    }

    protected void launchSettings() {
        final Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    protected void launchHelp() {
        final Intent intent = new Intent(this, Help.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    protected void launchAbout() {
        final Intent intent = new Intent(this, About.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private void sendText(String text) {
        String target = null; // use null instead of pref_destination_value_default
        if(mDestination.equals(getString(R.string.pref_destination_value_keep))) {
            target = PACKAGE_NAME_KEEP;
        } else if (mDestination.equals(getString(R.string.pref_destination_value_evernote))) {
            target = PACKAGE_NAME_EVERNOTE;
        } else if(mDestination.equals(getString(R.string.pref_destination_value_colornote))) {
            target = PACKAGE_NAME_COLORNOTE;
        }

        sendTextToTargetPackage(text, target);
    }

    private void sendTextToTargetPackage(String text, String target) {
        try
        {
            Intent intent = new Intent();

            if(null != target) {
                intent.setPackage(target);
            }

            intent.setAction(Intent.ACTION_SEND);
            intent.setType(MIMETYPE_TEXT_PLAIN);

            if(!EMPTY_TITLE) {
                intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.message_title));
            }
            if(!EMPTY_SUBJECT) {
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.message_subject));
            }
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.message_send_failed), Toast.LENGTH_SHORT).show();
        } catch (NullPointerException npe) {
            Toast.makeText(this, getString(R.string.message_send_failed), Toast.LENGTH_SHORT).show();
        }
    }

    public void startVoiceRecognitionActivity() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

            // This would be needed if recognizing in a different language than system default
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.speech_prompt));

            if(mEnableExtendedTimeout) {
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, COMPLETION_TIMEOUT);
            }

            startActivityForResult(intent, VOICE_NOTE_RETURN);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(this, getString(R.string.speech_fail), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.voicesearch_uri)));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e("startVoiceRecognition", getString(R.string.voicesearch_install_failure));
            }
            finish();
        }
    }
}