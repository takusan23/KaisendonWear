package io.github.takusan23.kaisendon;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.widget.EditText;
import android.widget.TextView;

public class SettingActivity extends WearableActivity {

    private SharedPreferences pref_setting;
    private EditText time_EditText;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref_setting.edit();

        time_EditText = findViewById(R.id.setting_time);

        //読み込み
        loadSetting(time_EditText,"time");

        // Enables Always-on
        setAmbientEnabled();
    }

    //Activity終了時に保存する
    @Override
    protected void onPause() {
        super.onPause();
        saveSetting(time_EditText,"time");
    }

    /**
     * 設定読み込み
     */
    private void loadSetting(EditText editText, String name) {
        editText.setText(pref_setting.getString(name, ""));
    }

    /**
     * 設定保存
     */
    private void saveSetting(EditText editText, String name) {
        editor.putString(name, editText.getText().toString());
        editor.apply();
    }

}
