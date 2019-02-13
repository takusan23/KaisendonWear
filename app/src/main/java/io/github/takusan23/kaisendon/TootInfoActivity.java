package io.github.takusan23.kaisendon;

import android.content.Intent;
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
import android.widget.LinearLayout;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TootInfoActivity extends WearableActivity {
    private SharedPreferences pref_setting;
    //あかうんと
    private String accessToken = "";
    private String instance = "";

    private TextView accountTextView;
    private ImageView accountImageView;
    private LinearLayout accountLinearLayout;
    private TextView tootTextView;
    private TextView timeTextView;

    private ImageButton favImageButton;
    private ImageButton boostImageButton;

    private LinearLayout imageLinearLayout;

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
        accountLinearLayout = findViewById(R.id.accountLinearLayout);
        tootTextView = findViewById(R.id.tootTextView);
        timeTextView = findViewById(R.id.timeTextView);

        favImageButton = findViewById(R.id.TootInfoFavImageButton);
        boostImageButton = findViewById(R.id.TootInfoBoostImageButton);

        imageLinearLayout = findViewById(R.id.tootInfoImageLinearLayout);

        frameLayout = findViewById(R.id.TootInfoFrameLayout);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(layoutParams);

        //読み込み
        tootInfo();

        //fav/btする
        favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favourited) {
                    TootAction(tootID, "unfavourite", favImageButton);
                    favourited = false;
                } else {
                    TootAction(tootID, "favourite", favImageButton);
                    favourited = true;
                }
            }
        });
        boostImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reblogged) {
                    TootAction(tootID, "unreblog", boostImageButton);
                    reblogged = false;
                } else {
                    TootAction(tootID, "reblog", boostImageButton);
                    reblogged = true;
                }
            }
        });

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
                        final String created_at = jsonObject.getString("created_at");
                        final String userID = jsonObject.getJSONObject("account").getString("id");
                        //時間表示変換
                        final String time = timeFormatChange(created_at, "9");
                        //画像
                        String imageURL_1 = null;
                        String imageURL_2 = null;
                        String imageURL_3 = null;
                        String imageURL_4 = null;
                        //画像表示
                        JSONArray media_array = jsonObject.getJSONArray("media_attachments");
                        if (!media_array.isNull(0)) {
                            imageURL_1 = media_array.getJSONObject(0).getString("url");
                        }
                        if (!media_array.isNull(1)) {
                            imageURL_2 = media_array.getJSONObject(1).getString("url");
                        }
                        if (!media_array.isNull(2)) {
                            imageURL_3 = media_array.getJSONObject(2).getString("url");
                        }
                        if (!media_array.isNull(3)) {
                            imageURL_4 = media_array.getJSONObject(3).getString("url");
                        }

                        final String finalImageURL_ = imageURL_1;
                        final String finalImageURL_1 = imageURL_2;
                        final String finalImageURL_2 = imageURL_3;
                        final String finalImageURL_3 = imageURL_4;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //UI
                                accountTextView.setText(display + "\n" + acct);
                                tootTextView.setText(toot);
                                timeTextView.setText(time);
                                //エミュレーターの場合は時間をしっかり合わせないと動かないよ
                                Glide.with(TootInfoActivity.this).load(avatar).into(accountImageView);

                                //画像読み込み
                                loadImage(finalImageURL_,new ImageView(TootInfoActivity.this));
                                loadImage(finalImageURL_1,new ImageView(TootInfoActivity.this));
                                loadImage(finalImageURL_2,new ImageView(TootInfoActivity.this));
                                loadImage(finalImageURL_3,new ImageView(TootInfoActivity.this));

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

                                //ユーザー情報に画面切り替え
                                accountLinearLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(TootInfoActivity.this,UserActivity.class);
                                        intent.putExtra("id",userID);
                                        startActivity(intent);
                                    }
                                });

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
        //Favouriteする
        String url = "https://" + instance + "/api/v1/statuses/" + id + "/" + endPoint + "/?access_token=" + accessToken;
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
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
                if (endPoint.contains("reblog")) {
                    Drawable boostIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                    boostIcon.setTint(Color.parseColor("#008000"));
                    imageButton.setImageDrawable(boostIcon);
                }
                if (endPoint.contains("favourite")) {
                    Drawable favIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_border_black_24dp_2, null);
                    favIcon.setTint(Color.parseColor("#ffd700"));
                    imageButton.setImageDrawable(favIcon);
                }
                if (endPoint.contains("unfavourite")) {
                    Drawable favIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_star_border_black_24dp_2, null);
                    favIcon.setTint(Color.parseColor("#ffffff"));
                    imageButton.setImageDrawable(favIcon);
                }
                if (endPoint.contains("unreblog")) {
                    Drawable boostIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                    boostIcon.setTint(Color.parseColor("#ffffff"));
                    imageButton.setImageDrawable(boostIcon);
                }
            }
        });
    }

    //時間変換
    /**
     * @param time    created_atの値
     * @param addTime 時間調整（例：９）
     */
    private String timeFormatChange(String time, String addTime) {
        String toot_time = time;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        //日本用フォーマット
        SimpleDateFormat japanDateFormat = new SimpleDateFormat(pref_setting.getString("pref_custom_time_format_text", "yyyy/MM/dd HH:mm:ss.SSS"), Locale.JAPAN);
        try {
            Date date = simpleDateFormat.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            //9時間足して日本時間へ
            calendar.add(Calendar.HOUR, +Integer.valueOf(addTime));
            //System.out.println("時間 : " + japanDateFormat.format(calendar.getTime()));
            toot_time = japanDateFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return toot_time;
    }

    //画像
    private void loadImage(String url, ImageView imageView) {
        if (url != null) {
            //ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //imageView.setLayoutParams(layoutParams);
            //呼び出し（こっわ
            if (imageView.getParent() != null) {
                ((ViewGroup) imageView.getParent()).removeView(imageView);
            }
            //読み込む
            Glide.with(TootInfoActivity.this).load(url).into(imageView);
            imageLinearLayout.addView(imageView);
        }
    }
}
