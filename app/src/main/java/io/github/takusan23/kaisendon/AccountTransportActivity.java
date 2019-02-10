package io.github.takusan23.kaisendon;

import android.accounts.Account;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.wear.activity.ConfirmationActivity;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class AccountTransportActivity extends WearableActivity implements
        MessageClient.OnMessageReceivedListener {


    private TextView mTextView;

    SharedPreferences pref_setting;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_transport);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref_setting.edit();
        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    /*
     *
     * アカウント情報受け取りはここでやる
     * */
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        //sendMessage var1 にいれた名前をequalsに入れる
        //instance
        if (messageEvent.getPath().equals("/instance")) {
            String message = new String(messageEvent.getData());
            //保存
            editor.putString("main_instance", message);
            editor.apply();
        }
        //accesstoken
        if (messageEvent.getPath().equals("/token")) {
            String message = new String(messageEvent.getData());
            //保存
            editor.putString("main_token", message);
            editor.apply();
        }
        //転送完了
        if (messageEvent.getPath().equals("/finish")) {
            //アカウント情報受け取ったら画面を切り替える
            // とーすと
            //Toast.makeText(AccountTransportActivity.this, R.string.transport_instance_ok, Toast.LENGTH_SHORT).show();
            Intent mainIntent = new Intent(AccountTransportActivity.this,MainActivity.class);
            //反応をわかりやすく
            Intent animation = new Intent(AccountTransportActivity.this, android.support.wearable.activity.ConfirmationActivity.class);
            animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE, android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION);
            animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.transport_instance_ok));

            startActivity(mainIntent);
            startActivity(animation);
            //なんかstartActivityで戻りたかったんだけど戻らないのでこのActivityを終了させることにする
            finish();
        }
    }
}
