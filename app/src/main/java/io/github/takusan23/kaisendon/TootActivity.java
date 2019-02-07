package io.github.takusan23.kaisendon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wear.activity.ConfirmationActivity;
import android.support.wear.widget.CircularProgressLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TootActivity extends WearableActivity {

    private SharedPreferences pref_setting;
    private EditText editText;
    private ImageView imageView;
    private CircularProgressLayout circularProgressLayout;
    //あかうんと
    private String accessToken = "";
    private String instance = "";

    //ImageViewのアイコンを変える
    private boolean circularProgressIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toot);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //アクセストークン
        accessToken = pref_setting.getString("main_token", "");
        instance = pref_setting.getString("main_instance", "");

        editText = findViewById(R.id.postEditText);
        imageView = findViewById(R.id.postImageView);
        circularProgressLayout = findViewById(R.id.circularProgress);

        //POST
        circularProgressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //アイコンチェンジ
                //カウントダウン終了時
                circularProgressLayout.setOnTimerFinishedListener(new CircularProgressLayout.OnTimerFinishedListener() {
                    @Override
                    public void onTimerFinished(CircularProgressLayout circularProgressLayout) {
                        //POST
                        UpdateStatus(editText.getText().toString(), "direct");
                    }
                });
                //キャンセル
                if (circularProgressIcon) {
                    //アイコンチェンジ
                    imageView.setImageDrawable(getDrawable(R.drawable.ic_send_black_24dp));
                    circularProgressIcon = false;
                    circularProgressLayout.stopTimer();
                } else {
                    imageView.setImageDrawable(getDrawable(R.drawable.ic_cancel_black_24dp));
                    circularProgressIcon = true;
                    //カウントダウン開始
                    //５秒
                    circularProgressLayout.setTotalTime(5000);
                    circularProgressLayout.startTimer();
                }
            }
        });

        // Enables Always-on
        setAmbientEnabled();
    }

    private void UpdateStatus(String text, String area) {
        String url = "https://" + instance + "/api/v1/statuses/?access_token=" + accessToken;
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .add("status", text)
                .add("visibility", area)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        //POST
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Intent intent = new Intent(TootActivity.this, MainActivity.class);
                //反応をわかりやすく
                Intent animation = new Intent(TootActivity.this, android.support.wearable.activity.ConfirmationActivity.class);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE, android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.toot_ok));

                startActivity(intent);
                startActivity(animation);
            }
        });
    }
}
