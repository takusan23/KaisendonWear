package io.github.takusan23.kaisendon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class TimelineAdapter extends ArrayAdapter<TimelineMenuItem> {

    private int mResource;
    private List<TimelineMenuItem> mItems;
    private LayoutInflater mInflater;

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

        if (convertView == null) {
            view = mInflater.inflate(R.layout.timeline_layout, parent, false);
            holder = new ViewHolder();

            holder.avatarImageView = view.findViewById(R.id.timelineImageView);
            holder.tootTextView = view.findViewById(R.id.timelineTextView);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // リストビューに表示する要素を取得
        TimelineMenuItem item = mItems.get(position);
        ArrayList<String> timelineItem = item.getList();


        //取得？
        String memo = timelineItem.get(0);
        String tootID = timelineItem.get(1);
        String tootText = timelineItem.get(2);
        String display_name = timelineItem.get(3);
        String acct = timelineItem.get(4);
        String avatarURL = timelineItem.get(5);
        String userID = timelineItem.get(5);

        //本文
        holder.tootTextView.setText(display_name + "/@" + acct + "\n" + tootText);

        //画像
        Glide.with(getContext())
                .load(avatarURL)
                .into(holder.avatarImageView);


        return view;
    }

    private class ViewHolder {
        TextView tootTextView;
        ImageView avatarImageView;
    }

}