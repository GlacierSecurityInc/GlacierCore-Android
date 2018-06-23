/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.glaciersecurity.glaciercore.core;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;

/*
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
*/

import com.glaciersecurity.glaciercore.R;
import com.glaciersecurity.glaciercore.activities.MainActivity;

public class ICSOpenVPNApplication extends Application {
    private StatusListener mStatus;

    @Override
    public void onCreate() {
        super.onCreate();
        PRNGFixes.apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

            createNotificationChannels();
        mStatus = new StatusListener();
        mStatus.init(getApplicationContext());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Background message
        CharSequence name = getString(R.string.channel_name_background);

        // GOOBER default "Connection statistics" toggle to be off if BYOD
        /* GOOBER BYOD default "Connection status change" toggle to be on/default
        IMPORTANCE_MAX: unused
        IMPORTANCE_HIGH: shows everywhere, makes noise and peeks
        IMPORTANCE_DEFAULT: shows everywhere, makes noise, but does not visually intrude
        IMPORTANCE_LOW: shows everywhere, but is not intrusive
        IMPORTANCE_MIN: only shows in the shade, below the fold
        IMPORTANCE_NONE: a notification with no importance; does not show in the shade */
        NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
                name, NotificationManager.IMPORTANCE_MIN);

        // GOOBER BYOD - Check if device is BYOD
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isBYOD = prefs.getBoolean("byod", false);

        if (isBYOD) {
            mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
                    name, NotificationManager.IMPORTANCE_NONE);
        }

        mChannel.setDescription(getString(R.string.channel_description_background));
        mChannel.enableLights(false);

        mChannel.setLightColor(Color.DKGRAY);
        mNotificationManager.createNotificationChannel(mChannel);

        // Connection status change messages

        name = getString(R.string.channel_name_status);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                name, NotificationManager.IMPORTANCE_DEFAULT);

        mChannel.setDescription(getString(R.string.channel_description_status));
        mChannel.enableLights(true);

        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);
    }
}