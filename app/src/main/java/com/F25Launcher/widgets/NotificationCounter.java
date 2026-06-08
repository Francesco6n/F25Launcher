package com.F25Launcher.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.F25Launcher.R;

public class NotificationCounter extends LinearLayout {

    private TextView mNotifCount;

    public NotificationCounter(@NonNull Context context) {
        super(context);
        init();
    }

    public NotificationCounter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.notification_count_view, this, false);
        mNotifCount = view.findViewById(R.id.notification_count);
        addView(view);
    }

    public void setCount(int count) {
        if (mNotifCount != null) {
            mNotifCount.setText(String.valueOf(count));
        }
    }
}
