package io.github.takusan23.kaisendon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.text.Html;
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

public class MainActivity extends WearableActivity {

    private SharedPreferences pref_setting;
    private ListView listView;
    private String timelineURL = "timelines/public?local=true&limit=40";

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

        //TL取得
        listView = findViewById(R.id.listView);
        loadTL();

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
}
