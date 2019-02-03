package io.github.takusan23.kaisendon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    SharedPreferences pref_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mTextView = (TextView) findViewById(R.id.text);

        //アカウント情報がなかったら取りに行く
        if (pref_setting.getString("main_token", "").length() == 0&&pref_setting.getString("main_instance", "").length() == 0) {
            Intent intent = new Intent(MainActivity.this, AccountTransportActivity.class);
            startActivity(intent);
        }

        mTextView.append("\n");
        mTextView.append("インスタンス : " + pref_setting.getString("main_instance",""));


        // Enables Always-on
        setAmbientEnabled();
    }


}
