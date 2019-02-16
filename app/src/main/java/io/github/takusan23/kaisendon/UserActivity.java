package io.github.takusan23.kaisendon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.LinkAddress;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.wear.widget.drawer.WearableActionDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserActivity extends WearableActivity implements MenuItem.OnMenuItemClickListener {

    private SharedPreferences pref_setting;
    //あかうんと
    private String accessToken = "";
    private String instance = "";
    //UserID
    private String userID;
    private String acct;

    private ImageView headerImageView;
    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView noteTextView;
    private Button followButton;
    private ListView listView;
    private Button created_at_TextView;
    private LinearLayout fieldsLinearLayout;

    ArrayList<TimelineMenuItem> toot_list;
    TimelineAdapter adapter;

    private FrameLayout frameLayout;
    private ProgressBar progressBar;

    //自分　それ以外
    private boolean myAccount = false;

    private WearableActionDrawerView mWearableActionDrawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Bottom Action Drawer
        mWearableActionDrawer =
                (WearableActionDrawerView) findViewById(R.id.userInfoMenuActionDrawer);
        // Peeks action drawer on the bottom.
        mWearableActionDrawer.getController().peekDrawer();
        mWearableActionDrawer.setOnMenuItemClickListener(this);


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
        created_at_TextView = findViewById(R.id.created_at);
        fieldsLinearLayout = findViewById(R.id.userInfofieldsLinearLayout);

        frameLayout = findViewById(R.id.UserFrameLayout);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(layoutParams);

        //読み込み
        loadAccount();

        //自分
        if (my != null) {
            followButton.setText(my);
        } else {
            //自分以外の場合、フォロー関係等を取りに行く
            accountRelationships();
        }


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
                    acct = jsonObject.getString("acct");
                    final String header = jsonObject.getString("header");
                    final String avatar = jsonObject.getString("avatar");
                    final String note = jsonObject.getString("note");
                    final String follow = jsonObject.getString("following_count");
                    final String follower = jsonObject.getString("followers_count");
                    final String statuses_count = jsonObject.getString("statuses_count");
                    final String created_at = timeFormatChange(jsonObject.getString("created_at"), "9");

                    //補足情報
                    //要素があるか確認
                    if (!jsonObject.getJSONArray("fields").isNull(0)) {
                        JSONArray fields = jsonObject.getJSONArray("fields");
                        for (int i = 0; i < fields.length(); i++) {
                            final String name = fields.getJSONObject(i).getString("name");
                            final String value = fields.getJSONObject(i).getString("value");
                            //レイアウトに入れる
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LinearLayout linearLayout = new LinearLayout(UserActivity.this);
                                    ViewGroup.LayoutParams linearlayout_layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    linearLayout.setLayoutParams(linearlayout_layoutParams);
                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                                    ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    ((LinearLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER;

                                    TextView name_textView = new TextView(UserActivity.this);
                                    TextView value_textView = new TextView(UserActivity.this);
                                    name_textView.setLayoutParams(layoutParams);
                                    value_textView.setLayoutParams(layoutParams);

                                    name_textView.setText(name);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        value_textView.setText(Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT));
                                    }
                                    linearLayout.addView(name_textView);
                                    linearLayout.addView(value_textView);
                                    linearLayout.setBackground(getDrawable(R.drawable.button_style));
                                    fieldsLinearLayout.addView(linearLayout);
                                }
                            });
                        }
                    }

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
                            //フォローとか書くよ
                            menuAddText(R.id.menu_follow, "\n" + follow);
                            menuAddText(R.id.menu_follower, "\n" + follower);
                            menuAddText(R.id.menu_status, "\n" + statuses_count);

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


    /**
     * 時間変換
     *
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


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int itemId = item.getItemId();
        Intent intent = new Intent(UserActivity.this, UserListActivity.class);
        switch (itemId) {
            case R.id.menu_status:
                //Toot
                intent.putExtra("id", userID);
                intent.putExtra("type", 1);
                startActivity(intent);
                break;
            case R.id.menu_follow:
                //Follow
                intent.putExtra("id", userID);
                intent.putExtra("type", 2);
                startActivity(intent);
                break;
            case R.id.menu_follower:
                //Follower
                intent.putExtra("id", userID);
                intent.putExtra("type", 3);
                startActivity(intent);
                break;
        }
        mWearableActionDrawer.getController().peekDrawer();
        return false;
    }

    /**
     * メニューにテキストを追加する
     *
     * @param id   メニューのID
     * @param text 追加したい文
     */
    private void menuAddText(int id, String text) {
        MenuItem menuItem = mWearableActionDrawer.getMenu().findItem(id);
        //一時保存
        String temp = menuItem.getTitle().toString();
        //入れる
        menuItem.setTitle(temp + text);
    }

    //フォロー、フォロー中、ふぉろば確認
    private void accountRelationships() {
        String url = "https://" + instance + "/api/v1/accounts/relationships/?stream=user&access_token=" + accessToken;
        //パラメータを設定
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        builder.addQueryParameter("id", String.valueOf(userID));
        String final_url = builder.build().toString();

        //作成
        Request request = new Request.Builder()
                .url(final_url)
                .get()
                .build();

        //GETリクエスト
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //入れる。なぜか配列になってる
                try {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    final boolean followed_by = jsonObject.getBoolean("followed_by");
                    //↓これ使うのかな・・？
                    boolean blocking = jsonObject.getBoolean("blocking");
                    boolean muting = jsonObject.getBoolean("muting");
                    final boolean following = jsonObject.getBoolean("following");
                    final String followed_by_string;
                    final String following_string;
                    //ふぉろば
                    if (followed_by) {
                        followed_by_string = getString(R.string.followed_by);
                    } else {
                        followed_by_string = getString(R.string.followed_by_not);
                    }
                    //フォローしているか
                    if (following) {
                        following_string = getString(R.string.following);
                    } else {
                        following_string = getString(R.string.follow_button);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            followButton.setText(following_string + "\n" + followed_by_string);
                            //アイコンを変える
                            //文字色も変える
                            if (following || followed_by) {
                                followButton.setTextColor(Color.parseColor("#87CEFA"));
                                Drawable follow_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done_black_24dp, null);
                                if (followed_by) {
                                    follow_icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done_all_black_24dp, null);
                                }
                                follow_icon.setTint(Color.parseColor("#87CEFA"));
                                followButton.setCompoundDrawablesRelativeWithIntrinsicBounds(follow_icon, null, null, null);
                            }

                            //フォローする
                            followButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //リモートフォローか分ける
                                    if (acct.contains("@")) {
                                        //リモートフォロー
                                        //ふぉろー
                                        if (!following) {
                                            remoteFollow(getString(R.string.follow_post));
                                        } else {
                                            //フォロー解除
                                            follow("unfollow", getString(R.string.unfollow_post));
                                        }
                                    } else {
                                        //ふぉろー
                                        if (!following) {
                                            follow("follow", getString(R.string.follow_post));
                                        } else {
                                            //フォロー解除
                                            follow("unfollow", getString(R.string.unfollow_post));
                                        }
                                    }
                                }
                            });

                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * フォローする（同じインスタンスの場合）
     *
     * @param followUrl "follow"または"unfollow"
     * @param message   POST終わったときに表示するメッセージ
     */
    private void follow(String followUrl, final String message) {
        String url = "https://" + instance + "/api/v1/accounts/" + userID + "/" + followUrl + "?access_token=" + accessToken;
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
                //反応をわかりやすく
                Intent animation = new Intent(UserActivity.this, android.support.wearable.activity.ConfirmationActivity.class);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE, android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE, message);
                startActivity(animation);
            }
        });
    }

    /**
     * リモートフォロー（違うインスタンスでのフォロー）
     *
     * @param message POST終わったときに表示するメッセージ
     */
    private void remoteFollow(final String message) {
        String url = "https://" + instance + "/api/v1/follows?access_token=" + accessToken;
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .add("uri", acct)
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
                //反応をわかりやすく
                Intent animation = new Intent(UserActivity.this, android.support.wearable.activity.ConfirmationActivity.class);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE, android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE, message);
                startActivity(animation);
            }
        });
    }


}
