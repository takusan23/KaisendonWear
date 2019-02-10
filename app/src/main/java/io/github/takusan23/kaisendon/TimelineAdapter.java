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
        final ViewHolder holder;
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
        //fav/reblog
        String favourited_string = timelineItem.get(14);
        String reblogged_string = timelineItem.get(15);
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
        final boolean finalFavourited = favourited;
        holder.favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fav済み？
                if (finalFavourited) {
                    TootAction(tootID, "unfavourite", holder.favImageButton);
                } else {
                    TootAction(tootID, "favourite", holder.favImageButton);
                }
            }
        });
        //Boostする
        final boolean finalReblogged = reblogged;
        holder.boostImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (finalReblogged) {
                    TootAction(tootID, "unreblog", holder.boostImageButton);
                } else {
                    TootAction(tootID, "reblog", holder.boostImageButton);
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
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                MastodonClient client = new MastodonClient.Builder(instance, new OkHttpClient.Builder(), new Gson()).accessToken(accessToken).build();
                RequestBody requestBody = new FormBody.Builder()
                        .build();
                client.post("statuses/" + id + "/" + endPoint, requestBody);
                return id;
            }

            @Override
            protected void onPostExecute(String result) {
/*
                if (endPoint.contains("reblog")) {
                    Drawable boostIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                    boostIcon.setTint(Color.parseColor("#008000"));
                    imageButton.setImageDrawable(boostIcon);
                }
                if (endPoint.contains("favourite")) {
                    Drawable favIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_border_black_24dp_2, null);
                    favIcon.setTint(Color.parseColor("#ffd700"));
                    imageButton.setImageDrawable(favIcon);
                }
                if (endPoint.contains("unfavourite")) {
                    Drawable favIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_star_border_black_24dp_2, null);
                    favIcon.setTint(Color.parseColor("#000000"));
                    imageButton.setImageDrawable(favIcon);
                }
                if (endPoint.contains("unreblog")) {
                    Drawable boostIcon = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_repeat_black_24dp_2, null);
                    boostIcon.setTint(Color.parseColor("#000000"));
                    imageButton.setImageDrawable(boostIcon);
                }
*/
            }
        }.execute();
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