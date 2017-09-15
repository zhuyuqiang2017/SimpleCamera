package com.appends.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.camera.CaptureLayoutHelper;

/**
 * Created by Administrator on 2017/9/3 0003.
 */

public class AppendSettingLayout extends RelativeLayout {
    public AppendSettingLayout(Context context) {
        super(context);
    }

    public AppendSettingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppendSettingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBounds(RectF area, CaptureLayoutHelper mHelper) {
        if(mHelper.shouldOverlayBottomBar()){
            if (area.width() > 0 && area.height() > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
                params.width = (int) area.width();
                params.height= (int) area.height()-(int) mHelper.getBottomBarRect().height();
                params.setMargins((int) area.left, (int) area.top, 0, 0);
                setLayoutParams(params);
            }
        }else{
            if (area.width() > 0 && area.height() > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
                params.width = (int) area.width();
                params.height= (int) area.height();
                params.setMargins((int) area.left, (int) area.top, 0, 0);
                setLayoutParams(params);
            }
        }
    }
}
