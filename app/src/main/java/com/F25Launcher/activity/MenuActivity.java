package com.F25Launcher.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.F25Launcher.R;
import com.F25Launcher.utils.LocaleHelper;

public class MenuActivity extends AppCompatActivity{
    private final static int[] title = { R.string.menu_set_wallpaper,R.string.menu_settings_launcher};
    private final static int[] summary = {0,0};
    private final int[] icon = {0,0};
    TextView back;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        ListView menu_view = findViewById(R.id.menu_list);
        back=findViewById(R.id.menu_back);
        //创建一个Adapter的实例
        MyBaseAdapter mAdapter = new MyBaseAdapter();
        //设置Adapter
        menu_view.setAdapter(mAdapter);
        menu_view.setOnItemClickListener(new mItemClick());
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        back.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the MainActivity
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }




    //创建一个类继承BaseAdapter
    class MyBaseAdapter extends BaseAdapter {
        //得到item的总数
        @Override
        public int getCount() {
            //返回ListView Item条目的总数
            return title.length;
        }
        //得到Item代表的对象
        @Override
        public Object getItem(int position) {
            //返回ListView Item条目代表的对象
            return title[position];
        }
        //得到Item的id
        @Override
        public long getItemId(int position) {
            //返回ListView Item的id
            return position;
        }
        //得到Item的View视图
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            //使用了懒汉模式
            if(convertView == null){
                //将list_item.xml文件找出来并转换成View对象
                convertView  = View.inflate(MenuActivity.this, R.layout.list_item, null);
                //找到list_item.xml中创建的TextView
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.menu_title);
                holder.summary = (TextView) convertView.findViewById(R.id.menu_summary);
                holder.icon = (ImageView) convertView.findViewById(R.id.menu_icon);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(title[position]);
            if (summary[position] != 0) {
                holder.summary.setText(summary[position]);
                holder.summary.setVisibility(View.VISIBLE);
            } else {
                holder.summary.setVisibility(View.GONE);
            }
            if (icon[position] != 0) {
                holder.icon.setBackgroundResource(icon[position]);
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
    static class ViewHolder{
        TextView title;
        TextView summary;
        ImageView icon;
    }
    class mItemClick implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (Math.toIntExact(id)){
                case 0:
                    Toast.makeText(MenuActivity.this,R.string.choose_wallpaper,Toast.LENGTH_LONG).show();
                    final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
                    Intent chooser = Intent.createChooser(pickWallpaper,"选择一个壁纸设置...\nSetup wallpaper...");
                    startActivity(chooser);
                    break;
                case 1:
                    Intent menu_it = new Intent(MenuActivity.this, SettingsActivity.class);
                    startActivity(menu_it);
                    break;
            }
        }
    }
}
