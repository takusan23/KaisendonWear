package io.github.takusan23.kaisendon;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.wearable.activity.WearableActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TootInfoActivity extends WearableActivity implements View.OnClickListener{
    private SharedPreferences pref_setting;
    //あかうんと
    private String accessToken = "";
    private String instance = "";

    private TextView accountTextView;
    private ImageView accountImageView;
    private TextView tootTextView;

    private ImageButton favImageButton;
    private ImageButton boostImageButton;

    private FrameLayout frameLayout;
    private ProgressBar progressBar;

    private String toot;
    private String userName;
    private String avatar;
    private String tootID;

    String favourited_string;
    String reblogged_string;
    boolean favourited = false;
    boolean reblogged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toot_info);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        accessToken = pref_setting.getString("main_token", "");
        instance = pref_setting.getString("main_instance", "");

        //IDを受け取る
        tootID = getIntent().getStringExtra("id");

        accountTextView = findViewById(R.id.accountTextView);
        accountImageView = findViewById(R.id.accountImageView);
        tootTextView = findViewById(R.id.tootTextView);

        favImageButton = findViewById(R.id.TootInfoFavImageButton);
        boostImageButton = findViewById(R.id.TootInfoBoostImageButton);

        frameLayout = findViewById(R.id.TootInfoFrameLayout);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(layoutParams);

        //読み込み
        tootInfo();

        //fav/btする
        favImageButton.setOnClickListener(this);
        boostImageButton.setOnClickListener(this);

        // Enables Always-on
        setAmbientEnabled();
    }

    private void tootInfo() {
        //くるくる（語彙力）をだす
        frameLayout.removeAllViews();
        frameLayout.addView(progressBar);
        String url = "https://" + instance + "/api/v1/statuses/" + tootID + "?access_token=" + accessToken;
        //作成
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();

        //GETリクエスト
        OkHttpClient client_1 = new OkHttpClient();
        client_1.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String response_string = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(response_string);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        final String display = jsonObject.getJSONObject("account").getString("display_name");
                        final String acct = jsonObject.getJSONObject("account").getString("acct");
                        final String avatar = jsonObject.getJSONObject("account").getString("avatar_static");
                        final String toot = Html.fromHtml(jsonObject.getString("content"), Html.FROM_HTML_MODE_COMPACT).toString();
                        final String toot_id = jsonObject.getString("id");
                        favourited_string = jsonObject.getString("favourited");
                        reblogged_string = jsonObject.getString("reblog");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //UI
                                accountTextView.setText(display + "\n" + acct);
                                tootTextView.setText(toot);
                                //エミュレーターの場合は時間をしっかり合わせないと動かないよ
                                Glide.with(TootInfoActivity.this).load(avatar).into(accountImageView);

                                if (Boolean.valueOf(favourited_string)) {
                                    favourited = true;
                                    Drawable favIcon = ResourcesCompat.getDrawable(TootInfoActivity.this.getResources(), R.drawable.ic_star_border_black_24dp_2, null);
                                    favIcon.setTint(Color.parseColor("#ffd700"));
                                    favImageButton.setImageDrawable(favIcon);
                                }
                                if (Boolean.valueOf(reblogged_string)) {
                                    reblogged = true;
                                    Drawable boostIcon = ResourcesCompat.getDrawable(TootInfoActivity.this.getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                                    boostIcon.setTint(Color.parseColor("#008000"));
                                    boostImageButton.setImageDrawable(boostIcon);
                                }
                                //くるくる終了
                                frameLayout.removeAllViews();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Favourite、ReblogをPOSTするためのメゾット
    public void TootAction(final String id, final String endPoint, final ImageButton imageButton) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                MastodonClient client = new MastodonClient.Builder(instance, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
                RequestBody requestBody = new FormBody.Builder()
                        .build();
                client.post("statuses/" + id + "/" + endPoint, requestBody);
                return id;
            }

            @Override
            protected void onPostExecute(String result) {
                if (endPoint.contains("reblog")) {
                    Drawable boostIcon = ResourcesCompat.getDrawable(TootInfoActivity.this.getResources(), R.drawable.ic_repeat_black_24dp, null);
                    boostIcon.setTint(Color.parseColor("#008000"));
                    imageButton.setImageDrawable(boostIcon);
                }
                if (endPoint.contains("favourite")) {
                    Drawable favIcon = ResourcesCompat.getDrawable(TootInfoActivity.this.getResources(), R.drawable.ic_star_border_black_24dp, null);
                    favIcon.setTint(Color.parseColor("#ffd700"));
                    imageButton.setImageDrawable(favIcon);
                }
                if (endPoint.contains("unfavourite")) {
                    Drawable favIcon = ResourcesCompat.getDrawable(TootInfoActivity.this.getResources(), R.drawable.ic_star_border_black_24dp, null);
                    favIcon.setTint(Color.parseColor("#000000"));
                    imageButton.setImageDrawable(favIcon);
                }
                if (endPoint.contains("unreblog")) {
                    Drawable boostIcon = ResourcesCompat.getDrawable(TootInfoActivity.this.getResources(), R.drawable.ic_repeat_black_24dp, null);
                    boostIcon.setTint(Color.parseColor("#000000"));
                    imageButton.setImageDrawable(boostIcon);
                }
            }
        }.execute();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.TootInfoFavImageButton:
                if (favourited) {
                    TootAction(tootID, "unfavourite", favImageButton);
                } else {
                    TootAction(tootID, "favourite", favImageButton);
                }
            case R.id.TootInfoBoostImageButton:
                if (reblogged) {
                    TootAction(tootID, "unreblog", boostImageButton);
                } else {
                    TootAction(tootID, "reblog", boostImageButton);
                }
        }
    }
}
