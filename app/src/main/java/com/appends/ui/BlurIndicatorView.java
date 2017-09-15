package com.appends.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class BlurIndicatorView extends View {

    private Paint mPaint;
    private float mDash = 15f;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    private float density;
    private float mBaseRadius = 200;
    private int mWidth;
    private int mHeight;
    public BlurIndicatorView(Context context) {
        super(context);
        init();
    }

    public BlurIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlurIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        density = getResources().getDisplayMetrics().density;
        mRadius = mBaseRadius;
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2*density);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mCenterY = mHeight/2f;
        mCenterX = mWidth/2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setPathEffect(new DashPathEffect(new float[]{mDash*(mRadius/mBaseRadius),mDash*(mRadius/mBaseRadius)},1));
        canvas.drawCircle(mCenterX,mCenterY,mRadius,mPaint);
    }

    public void setRadius(float radius){
        mRadius = radius;
        invalidate();
    }

    public void setCenter(float centerXRatio,float centerYRatio){
        mCenterX = centerXRatio*mWidth;
        mCenterY = centerYRatio*mHeight;
        invalidate();
    }

    public float getCurrentRadius(){
        return mRadius;
    }
}
