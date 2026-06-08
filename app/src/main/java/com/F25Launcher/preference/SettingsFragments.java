package com.F25Launcher.preference;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.*;

import com.F25Launcher.BuildConfig;
import com.F25Launcher.R;

import java.util.Objects;

public class SettingsFragments extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    public ListPreference clock_locate,clock_size,app_list_style,language;
    SwitchPreference callSmsSwitch;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean callSmsCounter = sharedPreferences.getBoolean("switch_preference_callsms_counter",false);
        clock_size.setSummary(clock_size.getValue());
        clock_locate.setSummary(clock_locate.getEntry());
        app_list_style.setSummary(app_list_style.getEntry());
        callSmsSwitch.setChecked(callSmsCounter);
        if ("app_language".equals(key)) {
            language.setSummary(language.getEntry());
            Activity activity = getActivity();
            if (activity != null) {
                activity.recreate();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        if(Build.VERSION.SDK_INT<28){ ((PreferenceGroup) Objects.requireNonNull(findPreference("preference_func"))).removePreference(findPreference("preference_main_default_launcher")); }
        clock_locate= getPreferenceScreen().findPreference("list_preference_clock_locate");
        clock_size=getPreferenceScreen().findPreference("list_preference_clock_size");
        app_list_style=getPreferenceScreen().findPreference("app_list_func");
        callSmsSwitch=getPreferenceScreen().findPreference("switch_preference_callsms_counter");
        language=getPreferenceScreen().findPreference("app_language");
        clock_size.setSummary(clock_size.getValue());
        clock_locate.setSummary(clock_locate.getEntry());
        app_list_style.setSummary(app_list_style.getEntry());
        language.setSummary(language.getEntry());
        sp.edit().putBoolean("dark_mode", false).apply();
        sp.edit().putBoolean("app_list_focus_zoom", true).apply();
        sp.edit().putBoolean("switch_preference_app_list_func", false).apply();
        sp.edit().putBoolean("hide_app", true).apply();
        Preference simpleMenu = findPreference("switch_preference_app_list_func");
        if (simpleMenu != null) simpleMenu.setVisible(false);
        Preference hiddenApps = findPreference("hide_app");
        if (hiddenApps != null) hiddenApps.setVisible(false);
        Preference versionPref = findPreference("app_version");
        if (versionPref != null) {
            String version = BuildConfig.VERSION_NAME;
            String[] parts = version.split("-");
            if (parts.length > 1) {
                versionPref.setSummary("v." + parts[1]);
            } else {
                versionPref.setSummary(version);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
