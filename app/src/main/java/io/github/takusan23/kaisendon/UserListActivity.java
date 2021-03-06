package io.github.takusan23.kaisendon;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import io.github.takusan23.kaisendon.Utilities.PrgressBarUtil;
import io.github.takusan23.kaisendon.Utilities.loadTimelineAPI;

public class UserListActivity extends WearableActivity {

    private ListView listView;
    private SharedPreferences pref_setting;
    ArrayList<TimelineMenuItem> toot_list;
    TimelineAdapter adapter;

    FrameLayout frameLayout;
    ProgressBar progressBar;

    //あかうんと
    private String accessToken = "";
    private String instance = "";

    //UserID
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        listView = findViewById(R.id.userListView);

        frameLayout = findViewById(R.id.userListFrameLayout);
        progressBar = new ProgressBar(this);
        PrgressBarUtil.kaisendonWearProgressBarShow(frameLayout, progressBar);

        //アクセストークン
        accessToken = pref_setting.getString("main_token", "");
        instance = pref_setting.getString("main_instance", "");

        toot_list = new ArrayList<>();
        adapter = new TimelineAdapter(this, R.layout.timeline_layout, toot_list);

        //UserIDを持ってくる
        userID = getIntent().getStringExtra("id");

        //Intentに載せたintで
        //Toot / Follow / Follower
        //を分ける

        //1 ユーザートゥート
        //2 ふぉろー　
        //3 ふぉろわー
        String url = "";
        switch (getIntent().getIntExtra("type", 1)) {
            case 1:
                url = "https://" + instance + "/api/v1/" + "accounts/" + userID + "/statuses" + "?access_token=" + accessToken;
                loadTimelineAPI.loadTimeline(UserListActivity.this, adapter, listView, url, null, frameLayout);
                break;
            case 2:
                url = "https://" + instance + "/api/v1/" + "accounts/" + userID + "/following" + "?access_token=" + accessToken;
                loadTimelineAPI.loadFollow(UserListActivity.this, adapter, listView, url, frameLayout);
                break;
            case 3:
                url = "https://" + instance + "/api/v1/" + "accounts/" + userID + "/followers" + "?access_token=" + accessToken;
                loadTimelineAPI.loadFollow(UserListActivity.this, adapter, listView, url, frameLayout);
                break;
        }


        // Enables Always-on
        setAmbientEnabled();
    }
}
