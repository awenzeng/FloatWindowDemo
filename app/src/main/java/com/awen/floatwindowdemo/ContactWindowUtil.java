package com.awen.floatwindowdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

/**
 * Created by AwenZeng on 2016/12/30.
 */

public class ContactWindowUtil {

    private ContactView contactView;
    private View dialogView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager.LayoutParams dialogParams;
    private Context mContext;
    private ContactWindowListener mListener;
    private ValueAnimator valueAnimator;
    private int direction;
    private final int LEFT = 0;
    private final int RIGHT = 1;

    public interface ContactWindowListener {
        void onDataCallBack(String str);
    }

    public void setDialogListener(ContactWindowListener listener) {
        mListener = listener;
    }

    //私有化构造函数
    public ContactWindowUtil(Context context) {
        mContext = context;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//      windowManager = (WindowManager) FloatWindowApp.getAppContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void showContactView() {
        hideContactView();
        contactView = new ContactView(mContext);
        if (layoutParams == null) {
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = contactView.width;
            layoutParams.height = contactView.height;
            layoutParams.x += ScreenSizeUtil.getScreenWidth();
            layoutParams.y += ScreenSizeUtil.getScreenHeight() - ScreenSizeUtil.dp2px(150);
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            if (Build.VERSION.SDK_INT > 18 && Build.VERSION.SDK_INT < 23) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.format = PixelFormat.RGBA_8888;
        }

        contactView.setOnTouchListener(touchListener);
        contactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContactDialog();
            }
        });
        windowManager.addView(contactView, layoutParams);
    }

    /**
     * 显示联系弹框
     */
    private void showContactDialog() {
        hideDialogView();
        if (dialogParams == null) {
            dialogParams = new WindowManager.LayoutParams();
            dialogParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialogParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            dialogParams.gravity = Gravity.CENTER;
            if (Build.VERSION.SDK_INT > 18 && Build.VERSION.SDK_INT < 25){
                dialogParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            } else {
                dialogParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            }
            dialogParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            dialogParams.format = PixelFormat.RGBA_8888;
        }
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogView = layoutInflater.inflate(R.layout.window_contact, null);
        TextView okTv = dialogView.findViewById(R.id.mOkTv);
        TextView cancleTv = dialogView.findViewById(R.id.mCancleTv);
        final TextView contentTv = dialogView.findViewById(R.id.mContentTv);
        contentTv.setText(String.format("您确认拨打%s客服电话吗", "4008-111-222"));
        cancleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialogView();
            }
        });
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialogView();
                mListener.onDataCallBack("");
            }
        });
        dialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideDialogView();
            }
        });
        windowManager.addView(dialogView, dialogParams);
    }


    public void hideAllView() {
        hideContactView();
        hideDialogView();
    }

    public void hideContactView() {
        if (contactView != null) {
            windowManager.removeView(contactView);
            contactView = null;
            stopAnim();
        }
    }

    public void hideDialogView() {
        if (dialogView != null) {
            windowManager.removeView(dialogView);
            dialogView = null;
        }
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        float startX;
        float startY;
        float moveX;
        float moveY;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();

                    moveX = event.getRawX();
                    moveY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX() - moveX;
                    float y = event.getRawY() - moveY;
                    //计算偏移量，刷新视图
                    layoutParams.x += x;
                    layoutParams.y += y;
                    windowManager.updateViewLayout(contactView, layoutParams);
                    moveX = event.getRawX();
                    moveY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    //判断松手时View的横坐标是靠近屏幕哪一侧，将View移动到依靠屏幕
                    float endX = event.getRawX();
                    float endY = event.getRawY();
                    if (endX < ScreenSizeUtil.getScreenWidth() / 2) {
                        direction = LEFT;
                        endX = 0;
                    } else {
                        direction = RIGHT;
                        endX = ScreenSizeUtil.getScreenWidth() - contactView.width;
                    }
                    if(moveX != startX){
                        starAnim((int) moveX, (int) endX,direction);
                    }
                    //如果初始落点与松手落点的坐标差值超过5个像素，则拦截该点击事件
                    //否则继续传递，将事件交给OnClickListener函数处理
                    if (Math.abs(startX - moveX) > 5) {
                        return true;
                    }
                    break;
            }
            return false;
        }
    };


    private void starAnim(int startX, int endX,final int direction) {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
        valueAnimator = ValueAnimator.ofInt(startX, endX);
        valueAnimator.setDuration(500);
        valueAnimator.setRepeatCount(0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(direction == LEFT){
                    layoutParams.x = (int) animation.getAnimatedValue()-contactView.width/2;
                }else{
                    layoutParams.x = (int) animation.getAnimatedValue();
                }
                if (contactView != null) {
                    windowManager.updateViewLayout(contactView, layoutParams);
                }
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
    }

    private void stopAnim() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }
}
