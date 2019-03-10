package io.github.takusan23.kaisendon.Utilities;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.takusan23.kaisendon.TimelineAdapter;
import io.github.takusan23.kaisendon.TimelineMenuItem;
import io.github.takusan23.kaisendon.UserListActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

public class loadTimelineAPI {


    //追加読み込み
    private static String lastID;
    private static boolean last = false;
    //スクロール位置保持
    private static int position;
    private static int y;
    //UserID
    private static int userID;

    /**
     * タイムラインを読み込むときに使います。<br>
     * timeline以外にもユーザーのstatusesにも使えます。
     *
     * @param activity        UIスレッド・Context用
     * @param timelineAdapter ListViewのAdapter
     * @param listView        ListView
     * @param url             APIのURL
     * @param maxID           追加読み込み。追加読込しない場合はnullを入れてね
     * @param frameLayout     くるくるを消すときに使う
     */
    public static void loadTimeline(final Activity activity, final TimelineAdapter timelineAdapter, final ListView listView, final String url, final String maxID, final FrameLayout frameLayout) {
        final ListView returnListView = listView;
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
                    //追加読み込み用リンクをヘッダーから持ってくる
                    final String header_url = response.headers().get("link");

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
                            activity.runOnUiThread(new Runnable() {
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
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            timelineAdapter.add(timelineMenuItem);
                                            timelineAdapter.notifyDataSetChanged();
                                            returnListView.setAdapter(timelineAdapter);
                                            last = false;
                                            //追加読み込み時にトップにワープしないようにする
                                            returnListView.setSelectionFromTop(position, y);


                                            //くるくる終了
                                            frameLayout.removeAllViews();

                                            //追加読み込みとか
                                            //System.out.println("数 " + adapter.getCount());

                                            returnListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                                @Override
                                                public void onScrollStateChanged(AbsListView view, int scrollState) {
                                                    position = returnListView.getFirstVisiblePosition();
                                                    y = returnListView.getChildAt(0).getTop();
                                                }

                                                @Override
                                                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                                    //これ最後だと無限に呼び出されるので最後一度だけ呼ばれるようにする
                                                    if (firstVisibleItem + visibleItemCount == totalItemCount && !last) {
                                                        last = true;
                                                        if (timelineAdapter.getCount() >= 20) {
                                                            //１個以上で動くように
                                                            //URLを正規表現で取る？
                                                            String url = null;
                                                            ArrayList<String> url_list = new ArrayList<>();
                                                            //正規表現実行
                                                            //判定するパターンを生成
                                                            Pattern p = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+");
                                                            Matcher m = p.matcher(header_url);
                                                            //正規表現で取り出す
                                                            //ループ
                                                            while (m.find()) {
                                                                url_list.add(m.group());
                                                            }

                                                            //max_idを配列から探す
                                                            //ないときは-1を返すのでちぇっく
                                                            if (url_list.get(0).contains("max_id")) {
                                                                url = url_list.get(0) + "&access_token=" + PreferenceManager.getDefaultSharedPreferences(activity).getString("main_token", "");
                                                                System.out.println("max_id りんく : " + url);
                                                                //実行
                                                                if (url != null) {
                                                                    loadTimelineAPI.loadTimeline(activity, timelineAdapter, listView, url, null, frameLayout);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    //frameLayout.removeAllViews();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //return returnListView;
    }

    /**
     * フォロー・フォロワーを取得するのに使います<br>
     * アクセストークンが必要
     *
     * @param activity        UIスレッド・Context用
     * @param timelineAdapter ListViewのAdapter
     * @param listView        ListView
     * @param url             APIのURL。パラメーター等はURLに入れた状態にしてね
     * @param frameLayout     くるくるを消すときに
     */

    public static void loadFollow(final Activity activity, final TimelineAdapter timelineAdapter, final ListView listView, final String url, final FrameLayout frameLayout) {
        final ListView returnListView = listView;
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
                    //追加読み込み用リンクをヘッダーから持ってくる
                    final String header_url = response.headers().get("link");

                    final JSONArray jsonArray = new JSONArray(response_string);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            final String acct = jsonArray.getJSONObject(i).getString("acct");
                            final String display = jsonArray.getJSONObject(i).getString("display_name");
                            final String accountID = jsonArray.getJSONObject(i).getString("id");
                            final String avatar = jsonArray.getJSONObject(i).getString("avatar");
                            final String note = jsonArray.getJSONObject(i).getString("note");
                            final String note_String = Html.fromHtml(note, Html.FROM_HTML_MODE_COMPACT).toString();


                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final ArrayList<String> arrayList = new ArrayList<>();
                                    //追加
                                    //配列用意
                                    //通知と分ける
                                    arrayList.add("");
                                    arrayList.add("userList");
                                    arrayList.add("");
                                    arrayList.add(note_String);
                                    arrayList.add(display);
                                    arrayList.add(acct);
                                    arrayList.add(avatar);
                                    arrayList.add(accountID);
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");
                                    arrayList.add("");

                                    final TimelineMenuItem timelineMenuItem = new TimelineMenuItem(arrayList);
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            timelineAdapter.add(timelineMenuItem);
                                            timelineAdapter.notifyDataSetChanged();
                                            returnListView.setAdapter(timelineAdapter);
                                            last = false;
                                            //くるくる終了
                                            frameLayout.removeAllViews();
                                            //追加読み込み時にトップにワープしないようにする
                                            returnListView.setSelectionFromTop(position, y);

                                            //追加読み込みとか
                                            returnListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                                @Override
                                                public void onScrollStateChanged(AbsListView view, int scrollState) {
                                                    position = returnListView.getFirstVisiblePosition();
                                                    y = returnListView.getChildAt(0).getTop();
                                                }

                                                @Override
                                                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                                    //これ最後だと無限に呼び出されるので最後一度だけ呼ばれるようにする
                                                    if (firstVisibleItem + visibleItemCount == totalItemCount && !last) {
                                                        //２回呼ばないようにする
                                                        last = true;
                                                        if (timelineAdapter.getCount() >= 20) {
                                                            //１個以上で動くように
                                                            //URLを正規表現で取る？
                                                            String url = null;
                                                            ArrayList<String> url_list = new ArrayList<>();
                                                            //正規表現実行
                                                            //判定するパターンを生成
                                                            Pattern p = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+");
                                                            Matcher m = p.matcher(header_url);
                                                            //正規表現で取り出す
                                                            //ループ
                                                            while (m.find()) {
                                                                url_list.add(m.group());
                                                            }

                                                            //max_idを配列から探す
                                                            //ないときは-1を返すのでちぇっく
                                                            if (url_list.get(0).contains("max_id")) {
                                                                url = url_list.get(0) + "&access_token=" + PreferenceManager.getDefaultSharedPreferences(activity).getString("main_token", "");
                                                                ;
                                                                System.out.println("max_id りんく : " + url);
                                                                //実行
                                                                if (url != null) {
                                                                    loadTimelineAPI.loadFollow(activity, timelineAdapter, listView, url, frameLayout);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    //frameLayout.removeAllViews();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //return returnListView;
    }


}
