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
import android.app.AlertDialog
import android.app.Dialog
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView

object VNStatic {
    // Manage the returned dialog when e.g. screen is rotated
    @JvmStatic
    fun showMessage(activity: Activity, message: SpannableString): Dialog {
        val altBld = AlertDialog.Builder(activity)
            .setCancelable(false)

        val linkMessage = SpannableString(message)
        Linkify.addLinks(linkMessage, Linkify.ALL) // Linkify.WEB_URLS

        altBld.setMessage(linkMessage)

        altBld.setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
            // Action for 'OK' Button
            dialog.dismiss()
            activity.finish()
        }

        val alert = altBld.create()
        alert.show()

        // Enable clickable links
        val text: TextView? = alert.findViewById(android.R.id.message)
        if (text != null) {
            text.movementMethod = LinkMovementMethod.getInstance()
            text.linksClickable = true
            text.autoLinkMask = Linkify.ALL
        }

        return alert
    }
}