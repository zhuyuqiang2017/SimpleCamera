package com.appends.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.android.camera.async.HandlerExecutor;

/**
 * Created by Administrator on 2017/9/10 0010.
 */

public class ManualSettingGroup extends ViewGroup {

    private int mChildWidth;
    private int mChildHeight;
    private boolean needCalculate = false;
    private int mLeftBorder;
    private int mRightBorder;
    private int mViewCount;
    private int mWidth;
    private int mHeight;
    private int start_position_X = 0;
    private int start_position_Y = 0;
    private int mMinScroll = 8;
    private float mDown;
    private float mLastDown;
    private float mMove;
    private Scroller mScroller;

    public ManualSettingGroup(Context context) {
        super(context);
        init();
    }

    public ManualSettingGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ManualSettingGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mScroller = new Scroller(getContext());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewCount = getChildCount();
        int width = 0;
        int height = 0;
        for (int i = 0; i < mViewCount ; i ++){
            View view = getChildAt(i);
            measureChild(view,widthMeasureSpec,heightMeasureSpec);
            width += view.getMeasuredWidth();
            height = Math.max(height,view.getMeasuredHeight());
        }
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        if(mWidth > width){
            mChildWidth = width;
            mChildHeight = height;
            needCalculate = true;
        }
        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed){
            if (needCalculate){
                calculateStartPosition();
            }
            int width = 0;
            for (int i = 0 ; i < mViewCount ; i ++){
                View view = getChildAt(i);
                view.layout(width+start_position_X,start_position_Y, start_position_X+width+view.getMeasuredWidth(),
                        start_position_Y+view.getMeasuredHeight());
                width += view.getMeasuredWidth();
            }
            mLeftBorder = getChildAt(0).getLeft();
            mRightBorder = getChildAt(mViewCount-1).getRight();
        }
    }

    private void calculateStartPosition() {
        start_position_X = (mWidth-mChildWidth)/2;
        start_position_Y = (mHeight-mChildHeight)/2;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDown = ev.getRawX();
                mLastDown = mDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mMove = ev.getRawX();
                int diff = (int) Math.abs(mMove-mLastDown);
                if(diff > mMinScroll){
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDown = event.getRawX();
                mLastDown = mDown;
                return true;
            case MotionEvent.ACTION_MOVE:
                mMove = event.getRawX();
                int diff = (int) (mLastDown - mMove)/2;
                if(getScrollX() + diff <= mLeftBorder){
                    diff = 0;
                }
                if(getScrollX() + diff + mWidth >= mRightBorder){
                    diff = 0;
                }
                scrollBy(diff,0);
                mLastDown = mMove;
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
        }
    }
}
