package com.F25Launcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.F25Launcher.R;

public class PowerOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showPowerDialog();
    }

    private void showPowerDialog() {
        String[] items = {getString(R.string.power_shutdown), getString(R.string.power_reboot)};
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(null)
                .setItems(items, (dialogInterface, which) -> {
                    switch (which) {
                        case 0:
                            shutdown();
                            break;
                        case 1:
                            reboot();
                            break;
                    }
                    finish();
                })
                .setOnCancelListener(dialogInterface -> finish())
                .create();
        dialog.show();
    }

    private void shutdown() {
        Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Intent fallback = new Intent(Intent.ACTION_SHUTDOWN);
            try {
                startActivity(fallback);
            } catch (Exception ignored) {
            }
        }
    }

    private void reboot() {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
