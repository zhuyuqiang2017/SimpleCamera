package com.appends.common;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2017/9/11 0011.
 */

public class DetailSettingItem {
    private String mSettingName;
    private int mNormalColor = Color.DKGRAY;
    private int mHighLightColor = Color.WHITE;
    private Drawable mNormalDrawable;
    private Drawable mHighLightDrawable;
    private boolean mIsSelected = false;
    private int mValue;
    public DetailSettingItem(String name){
        this.mSettingName = name;
    }

    public String getName(){
        return mSettingName;
    }

    public int getNormalColor() {
        return mNormalColor;
    }

    public void setNormalColor(int mNormalColor) {
        this.mNormalColor = mNormalColor;
    }

    public int getHighLightColor() {
        return mHighLightColor;
    }

    public void setHighLightColor(int mHighLightColor) {
        this.mHighLightColor = mHighLightColor;
    }

    public Drawable getNormalDrawable() {
        return mNormalDrawable;
    }

    public void setNormalDrawable(Drawable mNormalDrawable) {
        this.mNormalDrawable = mNormalDrawable;
    }

    public Drawable getHighLightDrawable() {
        return mHighLightDrawable;
    }

    public void setHighLightDrawable(Drawable mHighLightDrawable) {
        this.mHighLightDrawable = mHighLightDrawable;
    }

    public boolean isIsSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int mValue) {
        this.mValue = mValue;
    }
}
