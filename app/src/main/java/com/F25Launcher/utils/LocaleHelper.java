package com.F25Launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_LANGUAGE = "app_language";

    public static Context onAttach(Context context) {
        String lang = getPersistedLanguage(context);
        return setLocale(context, lang);
    }

    public static Context setLocale(Context context, String language) {
        persistLanguage(context, language);
        Locale locale;
        if (language == null || language.isEmpty() || language.equals("system")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(language);
        }
        return updateResources(context, locale);
    }

    private static Context updateResources(Context context, Locale locale) {
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        if (Build.VERSION.SDK_INT >= 24) {
            config.setLocales(new LocaleList(locale));
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            return context;
        }
    }

    private static String getPersistedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.launcherSettingsPref, Context.MODE_PRIVATE);
        return prefs.getString(PREF_LANGUAGE, "system");
    }

    private static void persistLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.launcherSettingsPref, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_LANGUAGE, language).apply();
    }
}
