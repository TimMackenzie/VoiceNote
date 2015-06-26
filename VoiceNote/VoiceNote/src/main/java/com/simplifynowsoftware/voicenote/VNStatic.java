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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class VNStatic {

    // Manage the returned dialog when e.g. screen is rotated
    public static Dialog showMessage(final Activity activity, final SpannableString message) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(activity);
        alt_bld.setCancelable(false);

        final SpannableString linkMessage = new SpannableString(message);
        Linkify.addLinks(linkMessage, Linkify.ALL); //Linkify.WEB_URLS

        alt_bld.setMessage(linkMessage);

        alt_bld.setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Action for 'OK' Button
                dialog.dismiss();
                activity.finish();
            }
        });

        AlertDialog alert = alt_bld.create();
        alert.show();

        // Enable clickable links
        TextView text = (TextView) alert.findViewById(android.R.id.message);
        if(null != text) {
            text.setMovementMethod(LinkMovementMethod.getInstance());
            text.setLinksClickable(true);
            text.setAutoLinkMask(Linkify.ALL);
        }

        return alert;
    }
}
