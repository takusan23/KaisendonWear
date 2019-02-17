package io.github.takusan23.kaisendon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.wear.activity.ConfirmationActivity;
import android.support.wear.widget.CircularProgressLayout;
import android.support.wear.widget.drawer.WearableActionDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.annotations.JsonAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TootActivity extends WearableActivity implements MenuItem.OnMenuItemClickListener {

    private SharedPreferences pref_setting;
    private EditText editText;
    private ImageView imageView;
    private CircularProgressLayout circularProgressLayout;
    private Button tootAreaImageButton;
    //あかうんと
    private String accessToken = "";
    private String instance = "";
    //公開範囲
    private String toot_area = "public";
    //Toot候補
    private WearableActionDrawerView mWearableActionDrawer;


    //ImageViewのアイコンを変える
    private boolean circularProgressIcon;

    @Override
    @SuppressLint("RestrictedApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toot);
        //設定読み込み
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //アクセストークン
        accessToken = pref_setting.getString("main_token", "");
        instance = pref_setting.getString("main_instance", "");

        editText = findViewById(R.id.postEditText);
        imageView = findViewById(R.id.postImageView);
        circularProgressLayout = findViewById(R.id.circularProgress);
        //Toot候補機能
        mWearableActionDrawer = findViewById(R.id.tootMenuActionDrawer);
        // Peeks action drawer on the bottom.
        mWearableActionDrawer.getController().peekDrawer();
        mWearableActionDrawer.setOnMenuItemClickListener(this);

        //POST
        circularProgressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //アイコンチェンジ
                //カウントダウン終了時
                circularProgressLayout.setOnTimerFinishedListener(new CircularProgressLayout.OnTimerFinishedListener() {
                    @Override
                    public void onTimerFinished(CircularProgressLayout circularProgressLayout) {
                        //POST
                        UpdateStatus(editText.getText().toString(), toot_area);
                        circularProgressLayout.stopTimer();
                        //Activity終了
                        finish();
                    }
                });
                //キャンセル
                if (circularProgressIcon) {
                    //アイコンチェンジ
                    imageView.setImageDrawable(getDrawable(R.drawable.ic_send_black_24dp));
                    circularProgressIcon = false;
                    circularProgressLayout.stopTimer();
                } else {
                    imageView.setImageDrawable(getDrawable(R.drawable.ic_cancel_black_24dp));
                    circularProgressIcon = true;
                    //カウントダウン開始
                    //５秒
                    circularProgressLayout.setTotalTime(5000);
                    circularProgressLayout.startTimer();
                }
            }
        });


        tootAreaImageButton = findViewById(R.id.postAreaImageButton);
        //ポップアップメニュー作成
        final MenuBuilder menuBuilder = new MenuBuilder(TootActivity.this);
        MenuInflater inflater = new MenuInflater(TootActivity.this);
        inflater.inflate(R.menu.toot_area_menu, menuBuilder);
        final MenuPopupHelper optionsMenu = new MenuPopupHelper(TootActivity.this, menuBuilder, tootAreaImageButton);
        optionsMenu.setForceShowIcon(true);
        // ポップアップメニューのメニュー項目のクリック処理
        tootAreaImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ポップアップメニューを表示
                optionsMenu.show();
                //反応
                menuBuilder.setCallback(new MenuBuilder.Callback() {
                    @Override
                    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.toot_area_public:
                                toot_area = "public";
                                tootAreaImageButton.setText(getString(R.string.visibility_public));
                                tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);
                                break;
                            case R.id.toot_area_unlisted:
                                toot_area = "unlisted";
                                tootAreaImageButton.setText(getString(R.string.visibility_unlisted));
                                tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_black_24dp, 0, 0, 0);
                                break;
                            case R.id.toot_area_local:
                                toot_area = "private";
                                tootAreaImageButton.setText(getString(R.string.visibility_private));
                                tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_black_24dp, 0, 0, 0);
                                break;
                            case R.id.toot_area_direct:
                                toot_area = "direct";
                                tootAreaImageButton.setText(getString(R.string.visibility_direct));
                                tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0);
                                break;
                        }
                        return false;
                    }

                    @Override
                    public void onMenuModeChange(MenuBuilder menuBuilder) {

                    }
                });
            }
        });


        //TootShortcut読み込み
        loadTootShortcut();


        // Enables Always-on
        setAmbientEnabled();
    }

    private void UpdateStatus(String text, String area) {
        String url = "https://" + instance + "/api/v1/statuses/?access_token=" + accessToken;
        //ぱらめーたー
        RequestBody requestBody = new FormBody.Builder()
                .add("status", text)
                .add("visibility", area)
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
                Intent intent = new Intent(TootActivity.this, MainActivity.class);
                //反応をわかりやすく
                Intent animation = new Intent(TootActivity.this, android.support.wearable.activity.ConfirmationActivity.class);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_ANIMATION_TYPE, android.support.wearable.activity.ConfirmationActivity.SUCCESS_ANIMATION);
                animation.putExtra(android.support.wearable.activity.ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.toot_ok));

                //startActivity(intent);
                startActivity(animation);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int itemId = item.getItemId();
        //トゥートショートカット設定の場合はActivity移動
        if (itemId == R.id.tootShortcutSetting) {
            Intent intent = new Intent(TootActivity.this, TootShortcutSettingActivity.class);
            startActivity(intent);
        } else {
            //テキスト入れる
            editText.setText(item.getTitle().toString());
            //公開範囲はアイコンから取る
            toot_area = drawableToString(item.getIcon());
        }
        return false;
    }

    //Toot候補を読み込む機能
    private void loadTootShortcut() {
        //String
        String tootshortcutText = pref_setting.getString("tootshortcutText", "");
        String tootshortcutIcon = pref_setting.getString("tootshortcutIcon", "");
        //Menu読み込み
        //エラー説
        Menu menu = mWearableActionDrawer.getMenu();

        //からじゃなかった
        if (!tootshortcutText.equals("")) {
            try {
                JSONArray text_JsonArray = new JSONArray(tootshortcutText);
                JSONArray icon_JsonArray = new JSONArray(tootshortcutIcon);
                //forで回す
                for (int i = 0; i < text_JsonArray.length(); i++) {
                    String text = text_JsonArray.getString(i);
                    String icon = icon_JsonArray.getString(i);
                    //Drawable
                    //メニュー追加
                    menu.add(0, i, 0, text).setIcon(stringToDrawable(icon));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //アイコン
    private Drawable stringToDrawable(String icon) {
        Drawable drawable = getDrawable(R.drawable.ic_public_black_24dp);
        switch (icon) {
            case "public":
                drawable = getDrawable(R.drawable.ic_public_black_24dp);
                break;
            case "unlisted":
                drawable = getDrawable(R.drawable.ic_done_all_black_24dp);
                break;
            case "private":
                drawable = getDrawable(R.drawable.ic_lock_open_black_24dp);
                break;
            case "direct":
                drawable = getDrawable(R.drawable.ic_assignment_ind_black_24dp);
                break;
        }
        return drawable;
    }

    //アイコンから文字列へ
    //ついでにアイコン変更
    private String drawableToString(Drawable icon) {
        String text = "public";
        if (icon.getCurrent().getConstantState().equals(getDrawable(R.drawable.ic_public_black_24dp).getConstantState())) {
            text = "public";
            tootAreaImageButton.setText(getString(R.string.visibility_public));
            tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_public_black_24dp, 0, 0, 0);
        }
        if (icon.getCurrent().getConstantState().equals(getDrawable(R.drawable.ic_done_all_black_24dp).getConstantState())) {
            text = "unlisted";
            tootAreaImageButton.setText(getString(R.string.visibility_unlisted));
            tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_done_all_black_24dp, 0, 0, 0);
        }
        if (icon.getCurrent().getConstantState().equals(getDrawable(R.drawable.ic_lock_open_black_24dp).getConstantState())) {
            text = "private";
            tootAreaImageButton.setText(getString(R.string.visibility_private));
            tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open_black_24dp, 0, 0, 0);
        }
        if (icon.getCurrent().getConstantState().equals(getDrawable(R.drawable.ic_assignment_ind_black_24dp).getConstantState())) {
            text = "direct";
            tootAreaImageButton.setText(getString(R.string.visibility_direct));
            tootAreaImageButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_assignment_ind_black_24dp, 0, 0, 0);
        }
        return text;
    }

}
