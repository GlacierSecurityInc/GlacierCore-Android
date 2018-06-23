/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.glaciersecurity.glaciercore.fragments;
import java.io.File;
import java.util.Collection;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.glaciersecurity.glaciercore.BuildConfig;
import com.glaciersecurity.glaciercore.Log;
import com.glaciersecurity.glaciercore.R;
import com.glaciersecurity.glaciercore.VpnProfile;
import com.glaciersecurity.glaciercore.api.ExternalAppDatabase;
import com.glaciersecurity.glaciercore.core.OpenVPNService;
import com.glaciersecurity.glaciercore.core.ProfileManager;


public class GeneralSettings extends PreferenceFragment implements OnPreferenceClickListener, OnClickListener, Preference.OnPreferenceChangeListener {

	private ExternalAppDatabase mExtapp;
	private ListPreference mAlwaysOnVPN;
    private CheckBoxPreference mBYOD;
    // final Preference byodPref = (Preference) findPreference("byod");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.general_settings);


        PreferenceCategory devHacks = (PreferenceCategory) findPreference("device_hacks");
		mAlwaysOnVPN = (ListPreference) findPreference("alwaysOnVpn");
        mBYOD = (CheckBoxPreference) findPreference("byod");

        // GOOBER
        // mAlwaysOnVPN.setOnPreferenceChangeListener(this);
        mBYOD.setOnPreferenceChangeListener(this);


        Preference loadtun = findPreference("loadTunModule");
		if(!isTunModuleAvailable()) {
			loadtun.setEnabled(false);
            devHacks.removePreference(loadtun);
        }

        CheckBoxPreference cm9hack = (CheckBoxPreference) findPreference("useCM9Fix");
        if (!cm9hack.isChecked() && (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            devHacks.removePreference(cm9hack);
        }

		mExtapp = new ExternalAppDatabase(getActivity());
		Preference clearapi = findPreference("clearapi");
		clearapi.setOnPreferenceClickListener(this);


        if(devHacks.getPreferenceCount()==0)
            getPreferenceScreen().removePreference(devHacks);

        if (!"ovpn3".equals(BuildConfig.FLAVOR)) {
            PreferenceCategory appBehaviour = (PreferenceCategory) findPreference("app_behaviour");
            appBehaviour.removePreference(findPreference("ovpn3"));
        }

        // GOOBER BYOD
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        boolean isBYOD = prefs.getBoolean("byod", false);
        if (isBYOD) {
            // GOOBER BYOD - have BYOD settings in Settings tab
            CheckBoxPreference byod = (CheckBoxPreference) findPreference("byod");
            byod.setChecked(true);
        }

        /* byodPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()) {
            public boolean onPreferenceClick(Preference preference){

            }
        });*/

		setClearApiSummary();
	}

	@Override
	public void onResume() {
		super.onResume();

        /* GOOBER - Removed so alwaysOnVPN takes current running profile
        ProfileManager pm = ProfileManager.getInstance(getActivity());
        Collection<VpnProfile> profiles = pm.getProfiles();
        CharSequence[] entries = new CharSequence[profiles.size()];
        CharSequence[] entryValues = new CharSequence[profiles.size()];;

        int i=0;
        for (VpnProfile p: profiles)
        {
            entries[i]=p.getName();
            entryValues[i]=p.getUUIDString();
            i++;
        }

        mAlwaysOnVPN.setEntries(entries);
        mAlwaysOnVPN.setEntryValues(entryValues);


        VpnProfile vpn = ProfileManager.getAlwaysOnVPN(getActivity());
		StringBuffer sb = new StringBuffer(getString(R.string.defaultvpnsummary));
		sb.append('\n');
        if (vpn== null)
            sb.append(getString(R.string.novpn_selected));
        else
           sb.append(getString(R.string.vpnselected, vpn.getName()));
		mAlwaysOnVPN.setSummary(sb.toString());*/

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference== mAlwaysOnVPN) {
            VpnProfile vpn = ProfileManager.get(getActivity(), (String) newValue);
            mAlwaysOnVPN.setSummary(vpn.getName());
        } else if (preference == mBYOD) {
            // GOOBER - set preferences for BYOD and ChannelNotification
            boolean checked = Boolean.valueOf(newValue.toString());

            Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton("Ok", this);
            builder.setTitle(getString(R.string.byod_restart_title));
            builder.setMessage(getString(R.string.byod_restart));
            builder.show();

			// GOOBER - Retrieve notification manager
			NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

			if (checked) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplication());
                prefs.edit().putBoolean("byod", true).apply();

                // GOOBER - Remove channel notificaetion if BYOD
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mNotificationManager.deleteNotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID);
                }
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplication());
                prefs.edit().putBoolean("byod", false).apply();

                // GOOBER - Add notification channel back into app.  Utilize Android O's new capability
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = getString(R.string.channel_name_background);
                    NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID, name, NotificationManager.IMPORTANCE_MIN);
                    mNotificationManager.createNotificationChannel(mChannel);
                }
            }
        }
        return true;
    }

    private void setClearApiSummary() {
		Preference clearapi = findPreference("clearapi");

		if(mExtapp.getExtAppList().isEmpty()) {
			clearapi.setEnabled(false);
			clearapi.setSummary(R.string.no_external_app_allowed);
		} else {
			clearapi.setEnabled(true);
			clearapi.setSummary(getString(R.string.allowed_apps,getExtAppList(", ")));
		}
	}

	private String getExtAppList(String delim) {
		ApplicationInfo app;
		PackageManager pm = getActivity().getPackageManager();

		String applist=null;
		for (String packagename : mExtapp.getExtAppList()) {
			try {
				app = pm.getApplicationInfo(packagename, 0);
				if (applist==null)
					applist = "";
				else
					applist += delim;
				applist+=app.loadLabel(pm);

			} catch (NameNotFoundException e) {
				// App not found. Remove it from the list
				mExtapp.removeApp(packagename);
			}
		}

		return applist;
	}

	private boolean isTunModuleAvailable() {
		// Check if the tun module exists on the file system
        return new File("/system/lib/modules/tun.ko").length() > 10;
    }

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getKey().equals("clearapi")){
			Builder builder = new AlertDialog.Builder(getActivity());
			builder.setPositiveButton(R.string.clear, this);
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.setMessage(getString(R.string.clearappsdialog,getExtAppList("\n")));
			builder.show();
		}

		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if( which == Dialog.BUTTON_POSITIVE){
			mExtapp.clearAllApiApps();
			setClearApiSummary();
		}
	}
}