package com.eric.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {

    /**
     *
     */
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    /**
     * 获取方向
     *
     * @param context
     * @return ORIENTATION_PORTRAIT 竖；ORIENTATION_LANDSCAPE 横
     */
    public static final int getOrientation(Context context) {
        Point screenResolution = getScreenWidthAndHeight(context);
        return screenResolution.x > screenResolution.y ? ORIENTATION_LANDSCAPE : ORIENTATION_PORTRAIT;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * sp 转pxs
     *
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取屏幕宽度和高度
     *
     * @param context
     * @return point.x 宽度；point.y 高度
     */
    public static Point getScreenWidthAndHeight(Context context) {
        Point point = new Point(0, 0);
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(dm);
            }
            int width = dm.widthPixels;         // 屏幕宽度（像素）
            int height = dm.heightPixels;       // 屏幕高度（像素）
            float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
            int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
            // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
            int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
            int screenHeight = (int) (height / density);// 屏幕高度(dp)
            point.set(screenWidth, screenHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return point;
    }

}
