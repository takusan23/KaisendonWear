package io.github.takusan23.kaisendon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TimelineAdapter extends ArrayAdapter<TimelineMenuItem> {

    private int mResource;
    private List<TimelineMenuItem> mItems;
    private LayoutInflater mInflater;
    //あかうんと
    private String accessToken = "";
    private String instance = "";
    private SharedPreferences pref_setting;

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param resource リソースID
     * @param items    リストビューの要素
     */
    public TimelineAdapter(Context context, int resource, List<TimelineMenuItem> items) {
        super(context, resource, items);
        mResource = resource;
        mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        //アクセストークン
        accessToken = pref_setting.getString("main_token", "");
        instance = pref_setting.getString("main_instance", "");

        if (convertView == null) {
            view = mInflater.inflate(R.layout.timeline_layout, parent, false);
            holder = new ViewHolder();

            holder.avatarImageView = view.findViewById(R.id.timelineImageView);
            holder.tootTextView = view.findViewById(R.id.timelineTextView);
            holder.nameTextView = view.findViewById(R.id.nametimelineTextView);
            holder.linearLayout = view.findViewById(R.id.timelineLinearLayout);
            holder.buttonLinearLayout = view.findViewById(R.id.buttonLinearLayout);
            holder.favImageButton = view.findViewById(R.id.favImageButton);
            holder.boostImageButton = view.findViewById(R.id.boostImageButton);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // リストビューに表示する要素を取得
        TimelineMenuItem item = mItems.get(position);
        final ArrayList<String> timelineItem = item.getList();


        //取得？
        String type = timelineItem.get(0);
        final String memo = timelineItem.get(1);
        final String tootID = timelineItem.get(2);
        String tootText = timelineItem.get(3);
        String display_name = timelineItem.get(4);
        String acct = timelineItem.get(5);
        String avatarURL = timelineItem.get(6);
        String userID = timelineItem.get(7);
        //reblog
        final String reblogToot = timelineItem.get(8);
        String reblogDisplayName = timelineItem.get(9);
        String reblogName = timelineItem.get(10);
        String reblogAvatar = timelineItem.get(11);
        String reblogAccountID = timelineItem.get(12);
        final String reblogTootID = timelineItem.get(13);

        String notificationType = "";
        if (!memo.isEmpty() && timelineItem.get(10) == null) {
            notificationType = "\n" + englishToJapanese(memo);
        }
        //名前
        holder.nameTextView.setText(display_name + " @" + acct + notificationType);

        //本文
        holder.tootTextView.setText(tootText);
        //画像
        Glide.with(getContext())
                .load(avatarURL)
                .into(holder.avatarImageView);

        //メモー通知のアイコン設定
        if (type != null) {
            if (type.contains("notification")) {
                switch (memo) {
                    case "mention":
                        holder.nameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_announcement_black_24dp, 0, 0, 0);
                        break;
                    case "favourite":
                        holder.nameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);
                        break;
                    case "reblog":
                        holder.nameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_black_24dp, 0, 0, 0);
                        break;
                    case "follow":
                        holder.nameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_black_24dp, 0, 0, 0);
                        break;
                }
            }
        }


        if (memo.contains("reblog")) {
            //BT内容を
            //名前
            holder.nameTextView.setText(reblogDisplayName + " @" + reblogName + "\n" + getContext().getString(R.string.reblog) + " : " + display_name + " @" + acct + notificationType);
            //本文
            holder.tootTextView.setText(reblogToot);
            //画像
            Glide.with(getContext()).load(reblogAvatar).into(holder.avatarImageView);
        }

        //Toot本文クリックしたら詳細画面へ飛ばす
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TootInfoActivity.class);
                //reblog / reblogじゃない
                if (memo.contains("reblog") && timelineItem.get(10) != null) {
                    intent.putExtra("id", reblogTootID);
                } else {
                    intent.putExtra("id", tootID);
                }
                getContext().startActivity(intent);
            }
        });

        //通知のMention以外の場合はレイアウト消す
        if (type != null) {
            if (type.contains("notification") && !memo.contains("mention")) {
                holder.buttonLinearLayout.removeAllViews();
            }
        }

        //Favouriteする
        holder.favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), memo, Toast.LENGTH_SHORT).show();
                //tootPOST("/favourite", tootID);
            }
        });
        //Boostする
        holder.boostImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tootPOST("/reblog", tootID);
            }
        });

        return view;
    }

    //mention→返信
    //に置き換えるプログラム
    private String englishToJapanese(String type) {
        String name = "";
        switch (type) {
            case "mention":
                name = getContext().getString(R.string.mention);
                break;
            case "favourite":
                name = getContext().getString(R.string.favourite);
                break;
            case "reblog":
                name = getContext().getString(R.string.reblog);
                break;
            case "follow":
                name = getContext().getString(R.string.follow);
                break;
        }
        return name;
    }

    //Favourite、ReblogをPOSTするためのメゾット
    private void tootPOST(String endPoint, final String id) {
        String url = "https://" + instance + "/api/v1/statuses/" + id + endPoint + "&access_token=" + accessToken;
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .build();
        //作成
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
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

                System.out.println("POSTしたよ" + response_string);
            }
        });

    }


    private class ViewHolder {
        TextView nameTextView;
        TextView tootTextView;
        ImageView avatarImageView;
        LinearLayout linearLayout;
        LinearLayout buttonLinearLayout;
        ImageButton favImageButton;
        ImageButton boostImageButton;
    }

}