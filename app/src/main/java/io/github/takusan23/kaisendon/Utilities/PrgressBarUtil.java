package io.github.takusan23.kaisendon.Utilities;

import android.icu.util.Freezable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import io.github.takusan23.kaisendon.R;

public class PrgressBarUtil {

    /*
     * くるくる
     */

    /**
     * くるくるを省略したやつ<br>
     * FrameLayoutのremoveAllViews<br>
     * ProgressBarのCenter表示<br>
     * FrameLayoutに追加を省略している。
     *
     * @param frameLayout レイアウト
     * @param progressBar くるくる
     */
    public static void kaisendonWearProgressBarShow(FrameLayout frameLayout, ProgressBar progressBar) {
        frameLayout.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
        frameLayout.addView(progressBar);
    }


}
