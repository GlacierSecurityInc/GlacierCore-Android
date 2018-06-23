package com.glaciersecurity.glaciercore.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.glaciersecurity.glaciercore.R;
import com.glaciersecurity.glaciercore.core.OpenVPNService;

public class BYODActivity extends BaseActivity {

    private RadioGroup byod_radioGroup;
    private Button byod_continue;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determine if this is the first time running the app by seeing if byod variable is defined
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.contains("byod")) {
            // go ahead and start app without prompting user for device type
            Intent i = new Intent(BYODActivity.this, MainActivity.class);
            startActivity(i);
        } else {
            // first time running app so prompt user to determine type of device (BYOD vs Coporate)
            setContentView(R.layout.byod_activity);

            byod_radioGroup = (RadioGroup) findViewById(R.id.byod_radioGroup);
            byod_continue = (Button) findViewById(R.id.continue_button);

            // change color of circle (and selecction circle) to white
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                int count = byod_radioGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    RadioButton x = (RadioButton) byod_radioGroup.getChildAt(i);
                    x.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
                }
            }

            // set Corporate to be seleccted by default
            ((RadioButton) byod_radioGroup.getChildAt(1)).setChecked(true);

            // continue button clicked
            byod_continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // retrieve which radio button clicked
                    int selectedId = byod_radioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    String tmpStr = (String) selectedRadioButton.getText();

                    // Retrieve notification manager
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    // set byod prefernce based on selected radiobutton
                    if (tmpStr.toLowerCase().contains("byod")) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        prefs.edit().putBoolean("byod", true).apply();

                        // Remove channel notificaetion if BYOD
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mNotificationManager.deleteNotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID);
                        }
                    } else {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        prefs.edit().putBoolean("byod", false).apply();

                        // Add notification channel back into app.  Utilize Android O's new capability
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            CharSequence name = getString(R.string.channel_name_background);
                            NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID, name, NotificationManager.IMPORTANCE_MIN);
                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                    }

                    // start main activity
                    Intent i = new Intent(BYODActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
        }
    }
}