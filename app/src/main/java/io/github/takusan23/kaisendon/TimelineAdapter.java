package io.github.takusan23.kaisendon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
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
import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
        mInflater = LayoutInflater.from(context);
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
            view = mInflater.inflate(R.layout.timeline_layout, null);
            holder = new ViewHolder();
            holder.avatarImageView = view.findViewById(R.id.timelineImageView);
            holder.tootTextView = view.findViewById(R.id.timelineTextView);
            holder.nameTextView = view.findViewById(R.id.nametimelineTextView);
            holder.linearLayout = view.findViewById(R.id.timelineLinearLayout);
            holder.buttonLinearLayout = view.findViewById(R.id.buttonLinearLayout);
            holder.favImageButton = view.findViewById(R.id.favImageButton);
            holder.boostImageButton = view.findViewById(R.id.boostImageButton);
            holder.imageLinearLayout = view.findViewById(R.id.imageLinearLayout);
            holder.mainLinearLayout = view.findViewById(R.id.timelineMainLinearLayout);

            holder.imageView1 = new ImageView(getContext());
            holder.imageView2 = new ImageView(getContext());
            holder.imageView3 = new ImageView(getContext());
            holder.imageView4 = new ImageView(getContext());

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // リストビューに表示する要素を取得
        TimelineMenuItem item = mItems.get(position);
        final ArrayList<String> timelineItem = item.getList();


        //取得？
        final String type = timelineItem.get(0);
        final String memo = timelineItem.get(1);
        final String tootID = timelineItem.get(2);
        String tootText = timelineItem.get(3);
        String display_name = timelineItem.get(4);
        String acct = timelineItem.get(5);
        String avatarURL = timelineItem.get(6);
        final String userID = timelineItem.get(7);
        //reblog
        final String reblogToot = timelineItem.get(8);
        String reblogDisplayName = timelineItem.get(9);
        String reblogName = timelineItem.get(10);
        String reblogAvatar = timelineItem.get(11);
        String reblogAccountID = timelineItem.get(12);
        final String reblogTootID = timelineItem.get(13);
        //fav/reblog
        String favourited_string = timelineItem.get(14);
        String reblogged_string = timelineItem.get(15);
        //Image URL
        String imageURL_1 = timelineItem.get(16);
        String imageURL_2 = timelineItem.get(17);
        String imageURL_3 = timelineItem.get(18);
        String imageURL_4 = timelineItem.get(19);

        boolean favourited = false;
        boolean reblogged = false;
        if (Boolean.valueOf(favourited_string)) {
            favourited = true;
/*
            Drawable favIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_border_black_24dp_2, null);
            favIcon.setTint(Color.parseColor("#ffd700"));
            holder.favImageButton.setImageDrawable(favIcon);
*/
        }
        if (Boolean.valueOf(reblogged_string)) {
            reblogged = true;
/*
            Drawable boostIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
            boostIcon.setTint(Color.parseColor("#008000"));
            holder.favImageButton.setImageDrawable(boostIcon);
*/
        }

        String notificationType = "";
        if (!memo.isEmpty() && timelineItem.get(10) == null) {
            notificationType = "\n" + englishToJapanese(memo);
        }
        //名前
        holder.nameTextView.setText(display_name + " @" + acct + notificationType);

        //本文
        holder.tootTextView.setText(tootText);
        //画像
        //非表示設定？
        if (pref_setting.getBoolean("image_load", false)) {
            //読み込む
            Glide.with(getContext()).load(avatarURL).into(holder.avatarImageView);
        } else {
            //レイアウトから消す
            holder.mainLinearLayout.removeView(holder.avatarImageView);
        }


        //画像
        if (pref_setting.getBoolean("image_load", false)) {
            loadImage(imageURL_1, holder.imageLinearLayout, holder.imageView1);
            loadImage(imageURL_2, holder.imageLinearLayout, holder.imageView2);
            loadImage(imageURL_3, holder.imageLinearLayout, holder.imageView3);
            loadImage(imageURL_4, holder.imageLinearLayout, holder.imageView4);
        }


        if (memo.contains("reblog")) {
            //BT内容を
            //名前
            holder.nameTextView.setText(reblogDisplayName + " @" + reblogName + "\n" + getContext().getString(R.string.reblog) + " : " + display_name + " @" + acct + notificationType);
            //本文
            holder.tootTextView.setText(reblogToot);
            //画像
            Glide.with(getContext()).load(reblogAvatar).into(holder.avatarImageView);
            holder.nameTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_black_24dp_2, 0, 0, 0);
        } else {
            holder.nameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

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

        //Toot本文クリックしたら詳細画面へ飛ばす
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ゆーざー情報・トゥート詳細
                if (type != null) {
                    if (memo.contains("userList")) {
                        //アカウント一覧のときはListを押すことでアカウントページに行けるようにする
                        Intent intent = new Intent(getContext(), UserActivity.class);
                        intent.putExtra("id", userID);
                        getContext().startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(getContext(), TootInfoActivity.class);
                    //reblog / reblogじゃない
                    if (memo.contains("reblog") && timelineItem.get(10) != null) {
                        intent.putExtra("id", reblogTootID);
                    } else {
                        intent.putExtra("id", tootID);
                    }
                    getContext().startActivity(intent);
                }

            }
        });

        //通知のMention以外の場合はレイアウト消す
        //あとアカウント一覧
        if (type != null) {
            if (type.contains("notification") && !memo.contains("mention") || memo.contains("userList")) {
                holder.buttonLinearLayout.removeAllViews();
                holder.imageLinearLayout.removeAllViews();
            }
        }

        //Favouriteする
        final boolean finalFavourited = favourited;
        final boolean[] clickFav = {false};
        final ImageButton favImageButton = holder.favImageButton;
        holder.favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fav済み？
                if (finalFavourited || clickFav[0]) {
                    TootAction(tootID, "unfavourite", favImageButton);
                    clickFav[0] = false;
                } else {
                    TootAction(tootID, "favourite", favImageButton);
                    clickFav[0] = true;
                }
            }
        });
        //Boostする
        final boolean finalReblogged = reblogged;
        final boolean[] clickBT = {false};
        final ImageButton btImageButton = holder.boostImageButton;
        holder.boostImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalReblogged || clickBT[0]) {
                    TootAction(tootID, "unreblog", btImageButton);
                    clickBT[0] = true;
                } else {
                    TootAction(tootID, "reblog", btImageButton);
                    clickBT[0] = false;
                }
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

            }
        });
    }

    //画像
    private void loadImage(String url, LinearLayout linearLayout, ImageView imageView) {
        if (url != null) {
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
            imageView.setLayoutParams(layoutParams);
            //呼び出し（こっわ
            if (imageView.getParent() != null) {
                ((ViewGroup) imageView.getParent()).removeView(imageView);
            }
            //読み込む
            Glide.with(getContext()).load(url).into(imageView);
            linearLayout.addView(imageView);
        } else {
            //本家版ではここの処理がいらないはずなんだけど
            //ListView再利用に巻き込まれていらないところに画像が表示されるので
            //見えないレベルでのImageViewを追加することにした。
            //もちろん何も読み込んでいない

            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
            imageView.setLayoutParams(layoutParams);
            //呼び出し（こっわ
            if (imageView.getParent() != null) {
                ((ViewGroup) imageView.getParent()).removeView(imageView);
            }
            //読み込む
            Glide.with(getContext()).load("").into(imageView);
            linearLayout.addView(imageView);
        }
    }

    private class ViewHolder {
        TextView nameTextView;
        TextView tootTextView;
        ImageView avatarImageView;
        LinearLayout mainLinearLayout;
        LinearLayout linearLayout;
        LinearLayout buttonLinearLayout;
        LinearLayout imageLinearLayout;
        ImageButton favImageButton;
        ImageButton boostImageButton;
        ImageView imageView1;
        ImageView imageView2;
        ImageView imageView3;
        ImageView imageView4;

    }

}