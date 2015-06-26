![VoiceNote](https://github.com/TimMackenzie/VoiceNote/raw/master/VoiceNote.png)


Introduction
------------

VoiceNote is a very simple application to demonstrate capturing the results of Google's voice recognition software.  It is set up for API 16+ because as of Android 4.1, the voice recognition occurs on the device.  That's pretty important, because relying on mobile data can make the voice recognition less reliable and the experience less fun.

I created this app just for myself upon getting a device with Android 4.1.  I didn't find an adequate solution to easily capture thoughts when I was, for example, at a stop light.  There are other options, but sometimes they have puzzling limitations (Google Now can fail to recognize voice when offline despite the voice recognition happening locally).

VoiceNote:
-Requires no internet connection, or any other permissions.
-Can be launched from the lock screen or anywhere.  It's not a widget, so it won't take any resources until it runs.
-Can be configured to send notes directly to a few popular note apps.

Since I created it for myself, the interface may not be to your liking - there's no main screen at all, which is not normal Android UX.  Go ahead and extend or modify it.  Use this code a a reference, as a starting point for your project, whatever you like.  It is licensed under Apache 2.0.

You can get this app already build from Google Play:
https://play.google.com/store/apps/details?id=com.simplifynowsoftware.voicenote

What does the app currently do?
-Start capturing immediatly upon start
-Respond to key words: help, preferences, settings, about
-Options:
--Select destination (Keep, Evernote, Colornote, or system picker)
--Options extend timeout if Google thinks you're done talking too soon


License
------------
  Copyright (C) 2013-2015 Simplify Now, LLC
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 
