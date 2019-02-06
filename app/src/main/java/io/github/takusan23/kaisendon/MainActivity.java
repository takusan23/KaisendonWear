package io.github.takusan23.kaisendon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wear.widget.drawer.WearableActionDrawerView;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.request.Request;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends WearableActivity implements MenuItem.OnMenuItemClickListener,AmbientModeSupport.AmbientCallbackProvider {

    private SharedPreferences pref_setting;
    private ListView listView;
    private String timelineURL = "timelines/public?local=true&limit=40";

    private String accessToken = "";

    //メニュー
    private WearableActionDrawerView mWearableActionDrawer;
    private WearableNavigationDrawerView mWearableNavigationDrawer;


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
        }

        //アクセストークン
        accessToken = pref_setting.getString("main_token","");


        //TL取得
        listView = findViewById(R.id.listView);
        loadTL();

        //NavigationDrawer
        mWearableNavigationDrawer = findViewById(R.id.top_navigation_drawer);
        mWearableNavigationDrawer.setAdapter(new NavigationAdapter(this));
        // Peeks navigation drawer on the top.
        mWearableNavigationDrawer.getController().peekDrawer();
        mWearableNavigationDrawer.addOnItemSelectedListener(new WearableNavigationDrawerView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                //コンテンツを切り替えたときはここ
                loadTL();
            }
        });

        // Bottom Action Drawer
        mWearableActionDrawer =
                (WearableActionDrawerView) findViewById(R.id.bottom_action_drawer);
        // Peeks action drawer on the bottom.
        mWearableActionDrawer.getController().peekDrawer();
        mWearableActionDrawer.setOnMenuItemClickListener(this);


        // Enables Always-on
        setAmbientEnabled();
    }


    private void loadTL() {
        ArrayList<TimelineMenuItem> toot_list = new ArrayList<>();
        final TimelineAdapter adapter = new TimelineAdapter(this, R.layout.timeline_layout, toot_list);
        adapter.clear();

        String url = "https://friends.nico/api/v1/" + timelineURL;
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
                    JSONArray jsonArray = new JSONArray(response_string);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            final String display = jsonArray.getJSONObject(i).getJSONObject("account").getString("display_name");
                            final String acct = jsonArray.getJSONObject(i).getJSONObject("account").getString("acct");
                            final String avatar = jsonArray.getJSONObject(i).getJSONObject("account").getString("avatar");
                            final String toot = Html.fromHtml(jsonArray.getJSONObject(i).getString("content"), Html.FROM_HTML_MODE_COMPACT).toString();
                            final String toot_id = jsonArray.getJSONObject(i).getString("id");
                            //TextView
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //追加
                                    //配列用意
                                    ArrayList<String> arrayList = new ArrayList<>();
                                    arrayList.add("");
                                    arrayList.add(toot_id);
                                    arrayList.add(toot);
                                    arrayList.add(display);
                                    arrayList.add(acct);
                                    arrayList.add(avatar);
                                    final TimelineMenuItem timelineMenuItem = new TimelineMenuItem(arrayList);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.add(timelineMenuItem);
                                            adapter.notifyDataSetChanged();
                                            listView.setAdapter(adapter);
                                        }
                                    });
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
                loadTL();
                break;
            case R.id.menu_toot:
/*
                Intent intent = new Intent(MainActivity.this, TootActivity.class);
                startActivity(intent);
*/
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
                    title = "ローカルTL";
                    break;
                case 3:
                    title = "連合TL";
                    break;
                case 4:
                    title = "設定";
                    break;
            }
            return title;
        }

        @Override
        public Drawable getItemDrawable(int pos) {
            //アイコンとか
            //これもサンプルからかけ離れた実装だから
            Drawable drawable = getDrawable(R.drawable.ic_send_black_24dp);
            switch (pos) {
                case 0:
                    drawable = getDrawable(R.drawable.ic_home_black_24dp);
                    timelineURL = "timelines/home?limit=40&access_token=" + accessToken;
                    break;
                case 1:
                    drawable = getDrawable(R.drawable.ic_notifications_black_24dp);
                    break;
                case 2:
                    drawable = getDrawable(R.drawable.ic_train_black_24dp);
                    timelineURL = "timelines/public?limit=40&access_token=" + accessToken + "&local=true";
                    break;
                case 3:
                    drawable = getDrawable(R.drawable.ic_flight_black_24dp);
                    timelineURL = "timelines/public?limit=40&access_token=" + accessToken + "&local=true";
                    break;
                case 4:
                    drawable = getDrawable(R.drawable.ic_settings_black_24dp);
                    break;
            }

            return drawable;
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
