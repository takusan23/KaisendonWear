package io.github.takusan23.kaisendon;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.wearable.activity.WearableActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class UserActivity extends WearableActivity {

    private SharedPreferences pref_setting;
    //あかうんと
    private String accessToken = "";
    private String instance = "";
    //UserID
    private String userID;

    private ImageView headerImageView;
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView noteTextView;
    private Button followButton;
    private ListView listView;
    private Button created_at_TextView;
    private Button followCount;
    private Button followerCount;
    private Button tootCount;

    ArrayList<TimelineMenuItem> toot_list;
    TimelineAdapter adapter;

    private FrameLayout frameLayout;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        //Intentからデータを貰う
        userID = getIntent().getStringExtra("id");
        //自分の場合
        String my = getIntent().getStringExtra("my");

        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        accessToken = pref_setting.getString("main_token", "");
        instance = pref_setting.getString("main_instance", "");

        headerImageView = findViewById(R.id.userHeader);
        headerImageView.setScaleType(ImageView.ScaleType.CENTER);
        avatarImageView = findViewById(R.id.userAvatar);
        nameTextView = findViewById(R.id.userNameTextView);
        noteTextView = findViewById(R.id.noteTextView);
        followButton = findViewById(R.id.followButton);
        followCount = findViewById(R.id.followCount);
        followerCount = findViewById(R.id.followerCount);
        tootCount = findViewById(R.id.tootCount);
        created_at_TextView = findViewById(R.id.created_at);

        frameLayout = findViewById(R.id.UserFrameLayout);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(layoutParams);

        //読み込み
        loadAccount();

        //自分
        if (my != null){
            followButton.setText(my);
        }

        //TL読み込み用
        toot_list = new ArrayList<>();
        adapter = new TimelineAdapter(this, R.layout.timeline_layout, toot_list);


        // Enables Always-on
        setAmbientEnabled();
    }

    private void loadAccount() {
        //くるくる（語彙力）をだす
        frameLayout.removeAllViews();
        frameLayout.addView(progressBar);
        String url = "https://" + instance + "/api/v1/accounts/" + userID;
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
                    final String display_name = jsonObject.getString("display_name");
                    final String acct = jsonObject.getString("acct");
                    final String header = jsonObject.getString("header");
                    final String avatar = jsonObject.getString("avatar");
                    final String note = jsonObject.getString("note");
                    final String follow = jsonObject.getString("following_count");
                    final String follower = jsonObject.getString("followers_count");
                    final String statuses_count = jsonObject.getString("statuses_count");
                    final String created_at = timeFormatChange(jsonObject.getString("created_at"), "9");

                    //UI Thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //名前
                            nameTextView.setText(display_name + "\n@" + acct);
                            //説明文
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                noteTextView.setText(Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT));
                            }
                            //数
                            followCount.setText(getString(R.string.follow_count) + " : " + follow);
                            followerCount.setText(getString(R.string.follower_count) + " : " + follower);
                            tootCount.setText(getString(R.string.toot_count) + " : " + statuses_count);
                            created_at_TextView.setText(getString(R.string.created_at) + " : " + created_at);

                            //画像
                            Glide.with(UserActivity.this).load(header).into(headerImageView);
                            Glide.with(UserActivity.this).load(avatar).into(avatarImageView);

                            //くるくる終了
                            frameLayout.removeAllViews();

                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
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


    //タイムラインの読み込み
    private void loadTL() {
        //くるくる（語彙力）をだす
        frameLayout.removeAllViews();
        frameLayout.addView(progressBar);

        String url = "https://" + instance + "/api/v1/accounts/" + userID + "/statuses?access_token=" + accessToken;
        //maxIDある？
        //パラメータを設定
        HttpUrl.Builder max_id_builder = HttpUrl.parse(url).newBuilder();
        String max_id_final_url = max_id_builder.build().toString();
        //作成
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(max_id_final_url)
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
                    final JSONArray jsonArray = new JSONArray(response_string);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            final String display = jsonArray.getJSONObject(i).getJSONObject("account").getString("display_name");
                            final String acct = jsonArray.getJSONObject(i).getJSONObject("account").getString("acct");
                            final String avatar = jsonArray.getJSONObject(i).getJSONObject("account").getString("avatar");
                            String toot_id = jsonArray.getJSONObject(i).getString("id");
                            final String accountID = jsonArray.getJSONObject(i).getJSONObject("account").getString("id");

                            //fav/reblog済み確認
                            String favourited = null;
                            String reblogged = null;

                            String reblogToot = null;
                            String reblogDisplayName = null;
                            String reblogName = null;
                            String reblogAvatar = null;
                            String reblogAccountID = null;
                            String reblogTootID = null;

                            //画像
                            String imageURL_1 = null;
                            String imageURL_2 = null;
                            String imageURL_3 = null;
                            String imageURL_4 = null;

                            //reblogとか
                            String type = "";
                            if (!jsonArray.getJSONObject(i).isNull("reblog")) {
                                type = "reblog";
                                JSONObject reblogJsonObject = jsonArray.getJSONObject(i).getJSONObject("reblog");
                                JSONObject reblogAccountJsonObject = reblogJsonObject.getJSONObject("account");
                                reblogToot = Html.fromHtml(reblogJsonObject.getString("content"), Html.FROM_HTML_MODE_COMPACT).toString();
                                reblogDisplayName = reblogAccountJsonObject.getString("display_name");
                                reblogName = reblogAccountJsonObject.getString("acct");
                                reblogAvatar = reblogAccountJsonObject.getString("avatar");
                                reblogAccountID = reblogAccountJsonObject.getString("id");
                                reblogTootID = reblogJsonObject.getString("id");
                            }


                            //通知とか
                            String toot = "";
                            String memo = null;
                            if (jsonArray.getJSONObject(i).has("content")) {
                                //通知以外
                                toot = Html.fromHtml(jsonArray.getJSONObject(i).getString("content"), Html.FROM_HTML_MODE_COMPACT).toString();
                                favourited = jsonArray.getJSONObject(i).getString("favourited");
                                reblogged = jsonArray.getJSONObject(i).getString("reblogged");
                                //画像表示
                                JSONArray media_array = jsonArray.getJSONObject(i).getJSONArray("media_attachments");
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

                            } else {
                                //通知
                                memo = "notification";
                                type = jsonArray.getJSONObject(i).getString("type");
                                toot_id = jsonArray.getJSONObject(i).getJSONObject("status").getString("id");
                                toot = Html.fromHtml(jsonArray.getJSONObject(i).getJSONObject("status").getString("content"), Html.FROM_HTML_MODE_COMPACT).toString();
                                //画像表示
                                JSONArray media_array = jsonArray.getJSONObject(i).getJSONObject("status").getJSONArray("media_attachments");
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
                            }
                            //TextView
                            final String finalToot = toot;
                            final String finalType = type;
                            final String finalReblogToot = reblogToot;
                            final String finalReblogDisplayName = reblogDisplayName;
                            final String finalReblogName = reblogName;
                            final String finalReblogAvatar = reblogAvatar;
                            final String finalReblogAccountID = reblogAccountID;
                            final String finalReblogTootID = reblogTootID;
                            final String finalToot_id = toot_id;
                            final String finalMemo = memo;
                            final String finalFavourited = favourited;
                            final String finalReblogged = reblogged;
                            final String finalImageURL_ = imageURL_1;
                            final String finalImageURL_1 = imageURL_2;
                            final String finalImageURL_2 = imageURL_3;
                            final String finalImageURL_3 = imageURL_4;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final ArrayList<String> arrayList = new ArrayList<>();
                                    //追加
                                    //配列用意
                                    //通知と分ける
                                    arrayList.add(finalMemo);
                                    arrayList.add(finalType);
                                    arrayList.add(finalToot_id);
                                    arrayList.add(finalToot);
                                    arrayList.add(display);
                                    arrayList.add(acct);
                                    arrayList.add(avatar);
                                    arrayList.add(accountID);
                                    arrayList.add(finalReblogToot);
                                    arrayList.add(finalReblogDisplayName);
                                    arrayList.add(finalReblogName);
                                    arrayList.add(finalReblogAvatar);
                                    arrayList.add(finalReblogAccountID);
                                    arrayList.add(finalReblogTootID);
                                    arrayList.add(finalFavourited);
                                    arrayList.add(finalReblogged);
                                    arrayList.add(finalImageURL_);
                                    arrayList.add(finalImageURL_1);
                                    arrayList.add(finalImageURL_2);
                                    arrayList.add(finalImageURL_3);

                                    final TimelineMenuItem timelineMenuItem = new TimelineMenuItem(arrayList);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.add(timelineMenuItem);
                                            adapter.notifyDataSetChanged();
                                            listView.setAdapter(adapter);
                                        }
                                    });
                                    frameLayout.removeAllViews();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
