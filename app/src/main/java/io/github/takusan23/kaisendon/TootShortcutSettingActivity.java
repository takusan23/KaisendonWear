package io.github.takusan23.kaisendon;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.wear.widget.drawer.WearableActionDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class TootShortcutSettingActivity extends WearableActivity implements MenuItem.OnMenuItemClickListener, MenuBuilder.Callback {
    private SharedPreferences pref_setting;

    private TextView textView;
    private EditText editText;
    private Button button;
    private Button areaButton;
    //削除リスト
    private WearableActionDrawerView mWearableActionDrawer;

    //メニューの配列
    private ArrayList<String> stringArrayList;
    private ArrayList<String> iconArrayList;
    //公開範囲
    private String toot_area = "public";
    private Menu tempMenu;

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toot_shortcut_setting);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        textView = findViewById(R.id.toot_shortcut_message_textView);
        editText = findViewById(R.id.toot_shortcut_editText);
        button = findViewById(R.id.toot_shortcut_button);
        areaButton = findViewById(R.id.toot_shortcut_area_button);

        // Bottom Action Drawer
        mWearableActionDrawer =
                (WearableActionDrawerView) findViewById(R.id.toot_shortcut_MenuActionDrawer);
        // Peeks action drawer on the bottom.
        mWearableActionDrawer.getController().peekDrawer();
        mWearableActionDrawer.setOnMenuItemClickListener(this);
        tempMenu = mWearableActionDrawer.getMenu();

        textView.setText(getString(R.string.tootShortcut_setting) + "\n" + getString(R.string.tootshortcut_message));

        //String → ArrayList
        stringArrayList = new ArrayList<>();
        iconArrayList = new ArrayList<>();
        stringToArrayList();

        //ポップアップメニュー作成
        final MenuBuilder menuBuilder = new MenuBuilder(TootShortcutSettingActivity.this);
        MenuInflater inflater = new MenuInflater(TootShortcutSettingActivity.this);
        inflater.inflate(R.menu.toot_area_menu, menuBuilder);
        final MenuPopupHelper optionsMenu = new MenuPopupHelper(TootShortcutSettingActivity.this, menuBuilder, areaButton);
        optionsMenu.setForceShowIcon(true);
        //公開範囲
        areaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポップアップメニューを表示
                optionsMenu.show();
                menuBuilder.setCallback(TootShortcutSettingActivity.this);
            }
        });


        //押したら追加する
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //EditTextから取り出す
                String text = editText.getText().toString();

                stringArrayList.add(text);
                iconArrayList.add(toot_area);

                //String変換And保存
                arrayListToStringAndSave();
                //再読込
                stringToArrayList();
            }
        });


        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        //削除
        if (item.getTitle().toString().contains(getString(R.string.toot_shortcut_delete_message))){
            stringArrayList.clear();
            iconArrayList.clear();
            //String変換And保存
            arrayListToStringAndSave();
            //再読込
            stringToArrayList();

        }else{
            //Textの削除はItemの本文から。
            //Iconの削除はindexOfで上から何個目かを図って削除
            int position = stringArrayList.indexOf(item.getTitle().toString());
            //なかったら-1
            if (position != -1){
                //削除
                stringArrayList.remove(position);
                iconArrayList.remove(position);
                Menu menu = mWearableActionDrawer.getMenu();
                menu.clear();
                getMenuInflater().inflate(R.menu.toot_shortcut_setting,menu);
                //String変換And保存
                arrayListToStringAndSave();
                //再読込
                stringToArrayList();
            }
        }
        return false;
    }

    //StringからArrayListへ変換するコード
    private void stringToArrayList() {
        //String
        String tootshortcutText = pref_setting.getString("tootshortcutText", "");
        String tootshortcutIcon = pref_setting.getString("tootshortcutIcon", "");

        //こいつら重要！！！！
        Menu menu = mWearableActionDrawer.getMenu();
        menu.clear();
        getMenuInflater().inflate(R.menu.toot_shortcut_setting,menu);
        stringArrayList.clear();
        iconArrayList.clear();

        //からじゃなかった
        //if (!tootshortcutText.contains("")) {
        try {
            JSONArray text_JsonArray = new JSONArray(tootshortcutText);
            JSONArray icon_JsonArray = new JSONArray(tootshortcutIcon);
            //forで回す
            for (int i = 0; i < text_JsonArray.length(); i++) {
                String text = text_JsonArray.getString(i);
                String icon = icon_JsonArray.getString(i);
                //配列に追加
                stringArrayList.add(text);
                iconArrayList.add(icon);
                //メニューにも入れる
                menu.add(0, i, 0, text).setIcon(R.drawable.ic_delete_black_24dp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // }
    }

    // ArrayListをStringにしてSharedPreferencesに保存する
    private void arrayListToStringAndSave() {
        JSONArray text_JsonArray = new JSONArray();
        JSONArray icon_JsonArray = new JSONArray();
        for (int i = 0; i < stringArrayList.size(); i++) {
            text_JsonArray.put(stringArrayList.get(i));
            icon_JsonArray.put(iconArrayList.get(i));
        }
        //保存
        SharedPreferences.Editor editor = pref_setting.edit();
        editor.putString("tootshortcutText", text_JsonArray.toString());
        editor.putString("tootshortcutIcon", icon_JsonArray.toString());
        editor.apply();

        //リストをからに
        stringArrayList.clear();
        iconArrayList.clear();
        //再読込み
        stringToArrayList();
    }

    /*
     *
     * 公開範囲変更
     * */
    @Override
    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.toot_area_public:
                toot_area = "public";
                areaButton.setText(getString(R.string.visibility_public));
                areaButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);
                break;
            case R.id.toot_area_unlisted:
                toot_area = "unlisted";
                areaButton.setText(getString(R.string.visibility_unlisted));
                areaButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_black_24dp, 0, 0, 0);
                break;
            case R.id.toot_area_local:
                toot_area = "private";
                areaButton.setText(getString(R.string.visibility_private));
                areaButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_black_24dp, 0, 0, 0);
                break;
            case R.id.toot_area_direct:
                toot_area = "direct";
                areaButton.setText(getString(R.string.visibility_direct));
                areaButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0);
                break;
        }
        return false;
    }

    @Override
    public void onMenuModeChange(MenuBuilder menuBuilder) {

    }
}
