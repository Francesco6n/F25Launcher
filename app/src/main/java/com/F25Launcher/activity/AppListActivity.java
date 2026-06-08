package com.F25Launcher.activity;

import static com.F25Launcher.utils.Constants.launcherSettingsPref;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.F25Launcher.BuildConfig;
import com.F25Launcher.R;
import com.F25Launcher.adapter.AppAdapter;
import com.F25Launcher.databinding.AppListActivityBinding;
import com.F25Launcher.icons.IconPack;
import com.F25Launcher.icons.providers.IconPackProvider;
import com.F25Launcher.utils.Application;
import com.F25Launcher.utils.Constants;
import com.F25Launcher.utils.LauncherUtils;
import com.F25Launcher.utils.LocaleHelper;
import com.F25Launcher.utils.PinyinComparator;
import com.F25Launcher.utils.PinyinUtils;
import com.F25Launcher.widgets.AppRecyclerView;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class AppListActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, AppAdapter.OnItemSelectCallback{
    private final static String TAG = AppListActivity.class.getSimpleName();
    private static final int DELAY_TIMER_MILLIS = 500;
    private static final int ACTIVITY_TRIGGER_COUNT = 3;
    private final long[] mHits = new long[ACTIVITY_TRIGGER_COUNT];
    private AppListActivityBinding binding;
    private PkgDelReceiver mPkgDelReceiver;
    private HideAppReceiver HideAppReceiver;
    private PinyinComparator mComparator;
    private SharedPreferences sharedPreferences;
    private String app_list_style;
    private String iconPackPkg;
    private List<String> excludePackagesList;
    private boolean isSimpleList;
    private boolean isFocusItemZoom;
    private boolean isSortByPinyin = false;
    private BroadcastReceiver menuKeyReceiver;
    private BroadcastReceiver powerLongPressReceiver;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AppListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.appBack.setOnClickListener(new funClick());
        binding.appMenu.setOnClickListener(new funClick());
        sharedPreferences = getSharedPreferences(launcherSettingsPref,Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        loadSettings(sharedPreferences);
        loadApp();
        receiveSyscast();
        menuKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showMenuDelayed();
            }
        };
        powerLongPressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent powerIt = new Intent(AppListActivity.this, PowerOptionsActivity.class);
                powerIt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(powerIt);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (menuKeyReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(menuKeyReceiver,
                    new IntentFilter(Constants.MENU_KEY_ACTION));
        }
        if (powerLongPressReceiver != null) {
            registerReceiver(powerLongPressReceiver, new IntentFilter("GWIN_POWER_LONG_PRESS"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (menuKeyReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(menuKeyReceiver);
        }
        if (powerLongPressReceiver != null) {
            try {
                unregisterReceiver(powerLongPressReceiver);
            } catch (Exception ignored) {
            }
        }
    }

    private void loadSettings(SharedPreferences sharedPreferences){
        app_list_style=sharedPreferences.getString("app_list_func","grid");
        isSimpleList=sharedPreferences.getBoolean("switch_preference_app_list_func",false);
        isSortByPinyin=sharedPreferences.getBoolean("switch_preference_app_list_sort",false);
        iconPackPkg = sharedPreferences.getString("pref_iconPackPackage", "android");
        isFocusItemZoom = sharedPreferences.getBoolean("app_list_focus_zoom",true);
        excludePackagesList = LauncherUtils.getExcludePackagesName(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadSettings(sharedPreferences);
    }

    private void receiveSyscast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        if (mPkgDelReceiver == null && HideAppReceiver == null) {
            mPkgDelReceiver = new PkgDelReceiver();
            HideAppReceiver = new HideAppReceiver();
            getApplicationContext().registerReceiver(mPkgDelReceiver, intentFilter);
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(HideAppReceiver,
                            new IntentFilter(Constants.HIDE_APP_ACTION));
        }
    }

    @Override
    public void onItemSelect(View v, int position) {
        Application application = ((AppAdapter) ((AppRecyclerView) v.getParent()).getAdapter()).getItem(position);
        if (application == null) {
            return;
        }
    }

    class PkgDelReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.e(TAG,"detect package change...");
            Toasty.info(context,R.string.refreshing_pkg_list,Toasty.LENGTH_SHORT).show();
            loadApp();
        }
    }

    class HideAppReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BuildConfig.DEBUG) Log.i(TAG, "Receive hide app list refresh broadcast");
            excludePackagesList = LauncherUtils.getExcludePackagesName(AppListActivity.this);
            Toasty.info(context,R.string.refreshing_pkg_list,Toasty.LENGTH_SHORT).show();
            loadApp();
        }
    }

    private void showMenuDelayed() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> showMenu(binding.appMenu), 150);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_SOFT_LEFT || event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                showMenu(binding.appMenu);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 3次按下数字键5触发
        if (keyCode == KeyEvent.KEYCODE_5) {
            arrayCopy();
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - DELAY_TIMER_MILLIS)) {
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setAction("com.F25Launcher.action.HIDE_APP_LIST");
                startActivity(intent);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void arrayCopy() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
    }

    class funClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v == binding.appBack) {
                finish();
            }else if(v == binding.appMenu){
                showMenu(v);
            }
        }
    }

    /**
     * 加载应用列表
     */
    private void loadApp(){
        List<Application> mApplicationList = new ArrayList<>();
        mComparator = new PinyinComparator();
        //设置启动Intent
        Intent intent = new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (ResolveInfo resolveInfo : getPackageManager().queryIntentActivities(intent, 0)) {
            String appLabel = resolveInfo.loadLabel(getPackageManager()).toString();
            String packageName = resolveInfo.activityInfo.packageName;
            boolean isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
            Intent appIntent = new Intent().setClassName(packageName, resolveInfo.activityInfo.name);
            Drawable appIcon = resolveInfo.loadIcon(getPackageManager());
            //如果应用的包名在排除列表内
            if (excludePackagesList != null && excludePackagesList.contains(packageName)) {
                continue;
            }
            if (BuildConfig.DEBUG) Log.d(TAG,"packageName: " + packageName);
            Application application;
            //如果使用图标包
            if (iconPackPkg.equals("android")){
                //初始化Application Bean
                application = new Application(
                        appIcon, //图标
                        resolveInfo.loadLabel(getPackageManager()), //名称
                        isSystemApp, //是否为系统应用
                        appIntent, //启动Intent
                        resolveInfo.activityInfo.packageName); //包名
            } else {
                //初始化Application Bean
                application = new Application(
                        LauncherUtils.getFromIconPack(this, appIcon, packageName), //图标
                        resolveInfo.loadLabel(getPackageManager()), //名称
                        isSystemApp, //是否为系统应用
                        appIntent, //启动Intent
                        resolveInfo.activityInfo.packageName); //包名
            }
            //如果使用按拼音排序
            if (isSortByPinyin) {
                String pinyin = PinyinUtils.getPingYin(appLabel);
                String sortString = pinyin.substring(0, 1).toUpperCase();
                //如果是字母开头
                if (sortString.matches("[A-Za-z]")) {
                    application.setLetters(sortString.toUpperCase());
                //否则设置Label为#
                } else {
                    application.setLetters("#");
                }
            }
            //如果启用了工具箱功能
            if (isSimpleList) {
                //排除自身和工具箱以及非系统应用
                if(!appLabel.equals(getString(R.string.app_name)) && isSystemApp || appLabel.equals(getString(R.string.trd_apps))){
                    mApplicationList.add(application);
                }
            } else {
                //否则只排除自身和工具箱
                if(!appLabel.equals(getString(R.string.app_name)) && !appLabel.equals(getString(R.string.trd_apps))){
                    mApplicationList.add(application);
                }
            }
        }
        addKaiOSApps(mApplicationList);

        //如果使用按拼音排序
        if (isSortByPinyin) {
            mApplicationList.sort(mComparator);
        }
        AppRecyclerView mAppRecyclerView = findViewById(R.id.app_list);
        AppAdapter appAdapter;
        //如果是网格布局
        if(app_list_style.equals("grid")){
            appAdapter = new AppAdapter(mApplicationList, 1, isFocusItemZoom);
            //      设置布局管理器
            mAppRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
            //      设置适配器
            mAppRecyclerView.setAdapter(appAdapter);
        }else{
            appAdapter = new AppAdapter(mApplicationList, 0, isFocusItemZoom);
            //列表布局
            mAppRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mAppRecyclerView.setAdapter(appAdapter);
        }
        appAdapter.setOnItemSelectCallback(this);
    }

    @SuppressLint("NonConstantResourceId")
    private void showMenu(View view){
        PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.app_option,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.menu_launcher_option:
                    Intent menu = new Intent(AppListActivity.this, MenuActivity.class);
                    menu.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(menu);
                    finish();
                    break;
                case R.id.menu_app_sort_pinyin:
                    isSortByPinyin = true;
                    sharedPreferences.edit().putBoolean("switch_preference_app_list_sort",true).apply();
                    loadApp();
                    break;
                case R.id.menu_app_sort_default:
                    isSortByPinyin = false;
                    sharedPreferences.edit().putBoolean("switch_preference_app_list_sort",false).apply();
                    loadApp();
                    break;
            }
            return true;
        });
        popupMenu.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPkgDelReceiver != null && HideAppReceiver != null){
            getApplicationContext().unregisterReceiver(mPkgDelReceiver);
            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(HideAppReceiver);
            mPkgDelReceiver = null;
            HideAppReceiver = null;
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void addKaiOSApps(List<Application> list) {
        List<Application> kaiosApps = new ArrayList<>();

        addKaiOSApp(kaiosApps, "Calcolatrice", "gwin.com.firefox", "gwin.com.firefox.calculator.CalculatorActivity", R.drawable.kaios_calculator);
        addKaiOSApp(kaiosApps, "Calendario", "gwin.com.firefox", "gwin.com.firefox.calendar.CalendarActivity", R.drawable.kaios_calendar);
        addKaiOSApp(kaiosApps, "Chiamate", "gwin.com.firefox", "gwin.com.firefox.calllog.CallLogActivity", R.drawable.kaios_calllog);
        addKaiOSApp(kaiosApps, "Fotocamera", "gwin.com.firefox", "gwin.com.firefox.camera.CameraActivity", R.drawable.kaios_camera);
        addKaiOSApp(kaiosApps, "Orologio", "gwin.com.firefox", "gwin.com.firefox.clock.ClockActivity", R.drawable.kaios_clock);
        addKaiOSApp(kaiosApps, "Contatti", "gwin.com.firefox", "gwin.com.firefox.contact.ContactActivity", R.drawable.kaios_contacts);
        addKaiOSApp(kaiosApps, "Telefono", "gwin.com.firefox", "gwin.com.firefox.launcher.DialActiviy", R.drawable.kaios_dialer);
        addKaiOSApp(kaiosApps, "Messaggi", "gwin.com.firefox", "gwin.com.firefox.mms.MessageActivity", R.drawable.kaios_messages);
        addKaiOSApp(kaiosApps, "Musica", "gwin.com.firefox", "gwin.com.firefox.music.MusicActivity", R.drawable.kaios_music);
        addKaiOSApp(kaiosApps, "Video", "gwin.com.firefox", "gwin.com.firefox.video.VideoActivity", R.drawable.kaios_video);
        addKaiOSApp(kaiosApps, "Radio FM", "gwin.com.firefox", "gwin.com.firefox.fm.FmActivity", R.drawable.kaios_fm);
        addKaiOSApp(kaiosApps, "Impostazioni", "gwin.com.firefox", "gwin.com.firefox.setting.SettingsActivity", R.drawable.kaios_settings);
        addKaiOSApp(kaiosApps, "Note", "gwin.com.firefox", "gwin.com.firefox.note.NoteActivity", R.drawable.kaios_note);
        addKaiOSApp(kaiosApps, "Browser", "com.android.browser", "com.android.browser.BrowserActivity", R.drawable.kaios_browser);
        addKaiOSApp(kaiosApps, "Registratore", "com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder", R.drawable.kaios_recorder);
        addKaiOSApp(kaiosApps, "Galleria", "com.android.gallery", "com.android.camera.GalleryPicker", R.drawable.kaios_gallery);

        for (Application app : kaiosApps) {
            int i = 0;
            while (i < list.size()) {
                if (list.get(i).getAppLabel().toString().compareToIgnoreCase(app.getAppLabel().toString()) > 0) {
                    break;
                }
                i++;
            }
            list.add(i, app);
        }
    }

    private void addKaiOSApp(List<Application> list, String label, String pkg, String cls, int iconResId) {
        Intent intent = new Intent();
        intent.setClassName(pkg, cls);
        Drawable icon = androidx.core.content.res.ResourcesCompat.getDrawable(getResources(), iconResId, getTheme());
        if (icon == null) {
            icon = getDrawable(android.R.drawable.sym_def_app_icon);
        }
        Application app = new Application(icon, label, true, intent, pkg);
        String pinyin = PinyinUtils.getPingYin(label);
        String sortString = pinyin.substring(0, 1).toUpperCase();
        if (sortString.matches("[A-Za-z]")) {
            app.setLetters(sortString.toUpperCase());
        } else {
            app.setLetters("#");
        }
        list.add(app);
    }
}
