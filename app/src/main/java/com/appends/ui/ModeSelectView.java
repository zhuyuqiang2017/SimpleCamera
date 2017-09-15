package com.appends.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuyuqiang on 2017/9/4.
 */

public class ModeSelectView extends ViewGroup {

    private int mViewCount;
    private int mWidth;
    private int mHeight;
    private List<Integer> mViewsWidth;
    private List<ModeItem> mItems;
    private ModeItem mCurrentModeItem;
    private int mStartPosition_x = 0;
    private int mStartPosition_y = 0;
    private int mScrollLimitation;
    private float mEventDown = 0.0f;
    private boolean requestRefresh = false;
    private OnCurrentModeItemChangeListener mListener;

    public interface OnCurrentModeItemChangeListener{
        void onCurrentModeItemChanged(ModeItem item);
    }
    public ModeSelectView(Context context) {
        super(context);
        init();
    }

    public ModeSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModeSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCurrentModeId(int modeId) {
        for (ModeItem item : mItems) {
            if (item.getModeId() == modeId) {
                mCurrentModeItem = item;
                mCurrentModeItem.setIsSelected(true);
                item.setIsSelected(true);
            } else {
                item.setIsSelected(false);
            }
        }
        requestLayout();
    }

    public void setModeId(int[] modeIds) {
        if (modeIds.length != mItems.size()) {
            throw new IllegalArgumentException("the size of modeIds is not equal the size of mItems");
        }
        for (int i = 0; i < mItems.size(); i++) {
            mItems.get(i).setModeId(modeIds[i]);
        }
    }

    private void init() {
        mViewsWidth = new ArrayList<>();
        mItems = new ArrayList<>();
        mScrollLimitation = 200;
    }

    public ModeItem getCurrentModeItem() {
        return mCurrentModeItem;
    }

    public void setCurrentModeItemChangeListener(OnCurrentModeItemChangeListener listener){
        this.mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewCount = getChildCount();
        if (mViewCount <= 0) {
            throw new RuntimeException("there is no childView in ModeSelectView");
        }

        int width = 0;
        int height = 0;
        for (int i = 0; i < mViewCount; i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
            width += view.getMeasuredWidth();
            mViewsWidth.add(i, view.getMeasuredWidth());
            height = Math.max(height, view.getMeasuredHeight());
            ModeItem item = new ModeItem(i);
            mItems.add(i, item);
        }
        switch (MeasureSpec.getMode(widthMeasureSpec)){
            case MeasureSpec.AT_MOST:
                mWidth = MeasureSpec.getSize(widthMeasureSpec);
                break;
            case MeasureSpec.UNSPECIFIED:
                mWidth = MeasureSpec.getSize(widthMeasureSpec);
                break;
            case MeasureSpec.EXACTLY:
                mWidth = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }

        switch (MeasureSpec.getMode(heightMeasureSpec)){
            case MeasureSpec.AT_MOST:
                mHeight = Math.min(MeasureSpec.getSize(heightMeasureSpec), height);
                break;
            case MeasureSpec.UNSPECIFIED:
                mHeight = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case MeasureSpec.EXACTLY:
                mHeight = MeasureSpec.getSize(heightMeasureSpec);
                break;
        }
        setMeasuredDimension(mWidth, mHeight);

    }

    @Override
    protected void onLayout(boolean b, int left, int top, int right, int bottom) {
        calculateCurrentPosition();
        int tempWidth = 0;
        for (int i = 0; i < mViewCount; i++) {
            View view = getChildAt(i);
            mStartPosition_y = (getHeight()-view.getMeasuredHeight())/2;
            view.layout(mStartPosition_x + tempWidth, mStartPosition_y, mStartPosition_x + tempWidth + view.getMeasuredWidth(), mStartPosition_y + view.getMeasuredHeight());
            tempWidth += view.getMeasuredWidth();
        }
        refreshHighLight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mEventDown = event.getX();
                requestRefresh = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = event.getX()-mEventDown;
                if(!requestRefresh){
                    if(distanceX < 0 && Math.abs((int)distanceX) > mScrollLimitation){
                        int index = mCurrentModeItem.getModeItemIndex();
                        if(index < (mViewCount-1)){
                            requestRefresh = true;
                            mCurrentModeItem.setIsSelected(false);
                            mCurrentModeItem = mItems.get(index+1);
                            mCurrentModeItem.setIsSelected(true);
                        }
                    }
                    if(distanceX > 0 && ((int)distanceX) > mScrollLimitation){
                        int index = mCurrentModeItem.getModeItemIndex();
                        if(index > 0){
                            requestRefresh = true;
                            mCurrentModeItem.setIsSelected(false);
                            mCurrentModeItem = mItems.get(index-1);
                            mCurrentModeItem.setIsSelected(true);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(requestRefresh){
                    requestLayout();
                    if(mListener != null){
                        mListener.onCurrentModeItemChanged(mCurrentModeItem);
                    }
                }
                mEventDown = 0.0f;
                requestRefresh = false;
                break;
        }
        return true;
    }

    private void refreshHighLight(){
        for (int i = 0 ; i < mViewCount ; i++){
            if(i == mCurrentModeItem.getModeItemIndex()){
                View view = getChildAt(i);
                ((TextView)view).setTextColor(Color.RED);
                ((TextView)view).setTextSize(32);
            }else{
                View view = getChildAt(i);
                ((TextView)view).setTextColor(Color.BLACK);
                ((TextView)view).setTextSize(24);
            }
        }
    }

    private void calculateCurrentPosition() {
        if (mCurrentModeItem == null) {
            mCurrentModeItem = new ModeItem(0, 0);
        }
        Log.i("zyq","currentIndex = "+mCurrentModeItem.getModeItemIndex());
        int width = 0;
        for (int i = mCurrentModeItem.getModeItemIndex(); i >= 0; i--) {
            View view = getChildAt(i);
            if (i == mCurrentModeItem.getModeItemIndex()) {
                width += view.getMeasuredWidth() / 2;
            } else {
                width += view.getMeasuredWidth();
            }
        }
        mStartPosition_x = getMeasuredWidth() / 2 - width;

    }

    private class ModeItem {
        int mModeItemIndex;
        int mModeId;
        boolean mIsSelected;

        public ModeItem(int modeItemIndex) {
            this.mModeItemIndex = modeItemIndex;
        }

        public ModeItem(int modeItemIndex, int modeId) {
            this.mModeItemIndex = modeItemIndex;
            this.mModeId = modeId;
        }

        public int getModeItemIndex() {
            return mModeItemIndex;
        }

        public void setModeItemIndex(int mModeItemIndex) {
            this.mModeItemIndex = mModeItemIndex;
        }

        public int getModeId() {
            return mModeId;
        }

        public void setModeId(int mModeId) {
            this.mModeId = mModeId;
        }

        public boolean getIsSelected() {
            return mIsSelected;
        }

        public void setIsSelected(boolean mIsSelected) {
            this.mIsSelected = mIsSelected;
        }
    }
}

