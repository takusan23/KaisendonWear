package io.github.takusan23.kaisendon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wear.widget.drawer.WearableActionDrawerView;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends WearableActivity implements MenuItem.OnMenuItemClickListener, AmbientModeSupport.AmbientCallbackProvider {

    private SharedPreferences pref_setting;
    //private RecyclerView recyclerView;
    private ListView listView;
    private String timelineURL = "";
    ArrayList<TimelineMenuItem> toot_list;
    TimelineAdapter adapter;
    //スクロール位置保持
    int position;
    int y;

    //あかうんと
    private String accessToken = "";
    private String instance = "";

    //名前
    private String name = "";
    private String userID = "";

    //メニュー
    private WearableActionDrawerView mWearableActionDrawer;
    private WearableNavigationDrawerView mWearableNavigationDrawer;
    private FrameLayout frameLayout;
    private ProgressBar progressBar;
    public static final int MY_ACCOUNT = 0;

    //追加読み込み
    private String lastID;
    private boolean listViewLast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //アカウント情報がなかったら取りに行く
        if (pref_setting.getString("main_token", "").length() == 0 && pref_setting.getString("main_instance", "").length() == 0) {
            Intent intent = new Intent(MainActivity.this, AccountTransportActivity.class);
            startActivity(intent);
            //このActivityを閉じる
            finish();
        }

        //アクセストークン
        accessToken = pref_setting.getString("main_token", "");
        instance = pref_setting.getString("main_instance", "");
        timelineURL = "timelines/home?limit=40&access_token=" + accessToken;


        //アカウント情報取得
        MyAccount();

        //FrameLayout
        frameLayout = findViewById(R.id.mainFrameLayout);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(layoutParams);

        //TL取得
        toot_list = new ArrayList<>();
        adapter = new TimelineAdapter(this, R.layout.timeline_layout, toot_list);
        listView = findViewById(R.id.listView);
        //recyclerView = findViewById(R.id.listView);
        loadTL(null);

        //NavigationDrawer
        mWearableNavigationDrawer = findViewById(R.id.top_navigation_drawer);
        mWearableNavigationDrawer.setAdapter(new NavigationAdapter(this));
        // Peeks navigation drawer on the top.
        mWearableNavigationDrawer.getController().peekDrawer();
        mWearableNavigationDrawer.addOnItemSelectedListener(new WearableNavigationDrawerView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                //コンテンツを切り替えたときはここ
                switch (i) {
                    case 0:
                        timelineURL = "timelines/home?limit=40&access_token=" + accessToken;
                        break;
                    case 1:
                        timelineURL = "notifications/?limit=40&access_token=" + accessToken;
                        break;
                    case 2:
                        timelineURL = "timelines/public?limit=40&access_token=" + accessToken + "&local=true";
                        break;
                    case 3:
                        timelineURL = "timelines/public?limit=40&access_token=" + accessToken;
                        break;
                }
                listViewLast = false;
                loadTL(null);
            }
        });

        // Bottom Action Drawer
        mWearableActionDrawer =
                (WearableActionDrawerView) findViewById(R.id.bottom_action_drawer);
        // Peeks action drawer on the bottom.
        mWearableActionDrawer.getController().peekDrawer();
        mWearableActionDrawer.setOnMenuItemClickListener(this);

        //画像読み込み設定読み込み
        loadSetting();

        // Enables Always-on
        setAmbientEnabled();
    }


    //タイムラインの読み込み
    /**
     * @param maxID 追加読み込み時に利用します。nullを入れると追加読み込み機能は無効になります。
     */
    private void loadTL(final String maxID) {
        final List<TimelineMenuItem> timeline = new ArrayList<>();
        //追加読み込み時はclearしない
        if (maxID == null) {
            adapter.clear();
        }
        //くるくる（語彙力）をだす
        frameLayout.removeAllViews();
        frameLayout.addView(progressBar);

        String url = "https://" + instance + "/api/v1/" + timelineURL;
        //maxIDある？
        //パラメータを設定
        HttpUrl.Builder max_id_builder = HttpUrl.parse(url).newBuilder();
        if (maxID != null) {
            max_id_builder.addQueryParameter("max_id", maxID);
        }
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
                                            listViewLast = false;
                                            if (maxID != null) {
                                                listView.setSelectionFromTop(position,y);
                                            }

                                            //追加読み込みとか
                                            //System.out.println("数 " + adapter.getCount());

                                            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                                @Override
                                                public void onScrollStateChanged(AbsListView view, int scrollState) {
                                                    position = listView.getFirstVisiblePosition();
                                                    y = listView.getChildAt(0).getTop();
                                                }

                                                @Override
                                                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                                    //これ最後だと無限に呼び出されるので最後一度だけ呼ばれるようにする
                                                    if (firstVisibleItem + visibleItemCount == totalItemCount && !listViewLast) {
                                                        if (adapter.getCount() >= 30) {
                                                            //Toast.makeText(MainActivity.this, "最後だよ", Toast.LENGTH_SHORT).show();
                                                            listViewLast = true;
                                                            //追加読み込み開始
                                                            try {
                                                                //最後（39個目）のToot IDを取得する
                                                                //通知は30件までしか取れないので条件分岐
                                                                if (finalMemo != null) {
                                                                    if (finalMemo.contains("notification")) {
                                                                        lastID = jsonArray.getJSONObject(29).getString("id");
                                                                    }
                                                                } else {
                                                                    lastID = jsonArray.getJSONObject(39).getString("id");
                                                                }
                                                                loadTL(lastID);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }

                                                        }
                                                    }
                                                }
                                            });
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


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int itemId = item.getItemId();
        //選択
        switch (itemId) {
            case R.id.menu_reload:
                loadTL(null);
                break;
            case R.id.menu_toot:
                Intent intent = new Intent(MainActivity.this, TootActivity.class);
                startActivity(intent);
                break;
            case MY_ACCOUNT:
                Intent accountIntent = new Intent(MainActivity.this,UserActivity.class);
                accountIntent.putExtra("id",userID);
                accountIntent.putExtra("my","あなたです！");
                startActivity(accountIntent);
                break;
            case R.id.image_load:
                //メニューのText変更
                //押したらON/OFFするように
                SharedPreferences.Editor editor = pref_setting.edit();
                if (pref_setting.getBoolean("image_load",false)){
                    //OFFにする
                    editor.putBoolean("image_load",false);
                    MenuItem menuItem = mWearableActionDrawer.getMenu().findItem(R.id.image_load);
                    String title = getString(R.string.image_load);
                    menuItem.setTitle("");
                    menuItem.setTitle(title + "\nOFF→ON");
                }else{
                    //ONにする
                    editor.putBoolean("image_load",true);
                    MenuItem menuItem = mWearableActionDrawer.getMenu().findItem(R.id.image_load);
                    String title = getString(R.string.image_load);
                    menuItem.setTitle("");
                    menuItem.setTitle(title + "\nON→OFF");
                }
                editor.apply();
                //reloadTL
                loadTL(null);
                break;
        }
        mWearableActionDrawer.getController().peekDrawer();
        return false;
    }


    //メニューにあるアイコン、タイトルの設定
    private final class NavigationAdapter
            extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

        private final Context mContext;

        public NavigationAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            //メニューの数？
            return 4;
        }

        @Override
        public String getItemText(int pos) {
            //なんかサンプルからかけ離れた実装方法だけどこれでいいわ
            String title = "ホーム";
            switch (pos) {
                case 0:
                    title = "ホーム";
                    break;
                case 1:
                    title = "通知";
                    break;
                case 2:
                    title = "ローカルタイムライン";
                    break;
                case 3:
                    title = "連合タイムライン";
                    break;
            }
            return title + "\n" + name;
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            //アイコンとか
            //これもサンプルからかけ離れた実装だから
            Drawable drawable = getDrawable(R.drawable.ic_home_black_24dp);
            switch (pos) {
                case 0:
                    drawable = getDrawable(R.drawable.ic_home_black_24dp);
                    break;
                case 1:
                    drawable = getDrawable(R.drawable.ic_notifications_black_24dp);
                    break;
                case 2:
                    drawable = getDrawable(R.drawable.ic_train_black_24dp);
                    break;
                case 3:
                    drawable = getDrawable(R.drawable.ic_flight_black_24dp);
                    break;
            }

            return drawable;
        }
    }

    //ID DisplayName
    private void MyAccount() {
        String url = "https://" + instance + "/api/v1/accounts/verify_credentials/?access_token=" + accessToken;
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
                    name = jsonObject.getString("display_name") + " @" + jsonObject.getString("acct");
                    userID = jsonObject.getString("id");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //自分の垢に移動するメニュー
                            mWearableActionDrawer.getMenu().add(0,MY_ACCOUNT,0,name).setIcon(R.drawable.ic_person_black_24dp);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //メニューのテキスト変更とか
    private void loadSetting(){
        if (pref_setting.getBoolean("image_load",false)){
            //OFFにする
            MenuItem menuItem = mWearableActionDrawer.getMenu().findItem(R.id.image_load);
            String title = getString(R.string.image_load);
            menuItem.setTitle("");
            menuItem.setTitle(title + "\nON→OFF");
        }else{
            //ONにする
            MenuItem menuItem = mWearableActionDrawer.getMenu().findItem(R.id.image_load);
            String title = getString(R.string.image_load);
            menuItem.setTitle("");
            menuItem.setTitle(title + "\nOFF→ON");
        }
    }


    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }


    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {
        /**
         * Prepares the UI for ambient mode.
         */
        @Override
        public void onEnterAmbient(Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);

            //mPlanetFragment.onEnterAmbientInFragment(ambientDetails);
            mWearableNavigationDrawer.getController().closeDrawer();
            mWearableActionDrawer.getController().closeDrawer();
        }

        /**
         * Restores the UI to active (non-ambient) mode.
         */
        @Override
        public void onExitAmbient() {
            super.onExitAmbient();
            //mPlanetFragment.onExitAmbientInFragment();
            mWearableActionDrawer.getController().peekDrawer();
        }
    }


}
