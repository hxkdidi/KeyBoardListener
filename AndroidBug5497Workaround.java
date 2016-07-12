package com.fullscreenlibs;

/**
 * Created by 95 on 2016/5/2.
 */
//Workaround to get adjustResize functionality for input methos when the fullscreen mode is on
//found by Ricardo
//taken from http://stackoverflow.com/a/19494006

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;


public class AndroidBug5497Workaround {
    private int offset=0;

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    public static void assistActivity(Activity activity, OnInputMethodManagerLinstener linstener) {
        new AndroidBug5497Workaround(activity, linstener);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private AndroidBug5497Workaround(final Activity activity, final OnInputMethodManagerLinstener linstener) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent(activity, linstener);
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent(Activity activity, OnInputMethodManagerLinstener linstener) {
        int usableHeightNow = computeUsableHeight(activity);
        if (usableHeightNow != usableHeightPrevious) {
            int sta = ScreenUtils.getStatusHeight(activity);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                sta = 0;
            }
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference-Math.abs(offset);
                if (linstener != null) {
                    linstener.inputMethodCallBack(heightDifference, true);
                }
                Log.e("observeSoftKeyboard", "键盘弹出 ");
            } else {
                // keyboard probably just became hidden  去掉顶部高度
                int a = ScreenUtils.getNavigationBarHeight(activity);
                if (heightDifference<0){
                    offset = heightDifference;
                }
                if (a==0){  //没有系统底部状态栏
                    heightDifference=0;
                }
                frameLayoutParams.height = usableHeightSansKeyboard - sta-heightDifference;  //  减去 底部导航栏高度
                Log.e("observeSoftKeyboard", "键盘隐藏 ");
                if (linstener != null) {
                    linstener.inputMethodCallBack(heightDifference, false);
                }
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight(Activity activity) {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        int sta = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sta = ScreenUtils.getStatusHeight(activity);
        }
        return (r.bottom - r.top + sta);
    }


    /**
     * 键盘监听接口
     */
    public interface OnInputMethodManagerLinstener {
        /**
         * 键盘状态接口
         *
         * @param inputHeight 键盘高度
         * @param isShow      是否显示
         */
        public void inputMethodCallBack(int inputHeight, boolean isShow);
    }

}
