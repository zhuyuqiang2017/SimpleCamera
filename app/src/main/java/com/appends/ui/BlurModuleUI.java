package com.appends.ui;

/**
 * Created by Administrator on 2017/9/3 0003.
 */

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.android.camera.CameraActivity;
import com.android.camera.CaptureLayoutHelper;
import com.android.camera.debug.Log;
import com.android.camera.ui.CountDownView;
import com.android.camera.ui.PreviewOverlay;
import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.ProgressOverlay;
import com.android.camera.ui.focus.FocusRing;
import com.android.camera2.R;

/**
 * Contains the UI for the CaptureModule.
 */
public class BlurModuleUI implements PreviewStatusListener.PreviewAreaChangedListener {

    public interface BlurModuleUIListener {
        public void onZoomRatioChanged(float zoomRatio);
    }

    private static final Log.Tag TAG = new Log.Tag("BlurModuleUI");

    private final CameraActivity mActivity;
    private final BlurModuleUIListener mListener;
    private final View mRootView;

    private final PreviewOverlay mPreviewOverlay;
    private final ProgressOverlay mProgressOverlay;
    private final TextureView mPreviewView;

    private final FocusRing mFocusRing;
    private final CountDownView mCountdownView;

    private int mPreviewAreaWidth;
    private int mPreviewAreaHeight;
    private AppendSettingLayout mAppendSetting;
    private CaptureLayoutHelper mCaptureLayoutHelper;
    private BlurIndicatorView mBlurIndicatorView;
    private SeekBar mSeekBar;

    /** Maximum zoom; intialize to 1.0 (disabled) */
    private float mMaxZoom = 1f;

    /** Set up listener to receive zoom changes from View and send to module. */
    private final PreviewOverlay.OnZoomChangedListener mZoomChangedListener = new PreviewOverlay.OnZoomChangedListener() {
        @Override
        public void onZoomValueChanged(float ratio) {
            mListener.onZoomRatioChanged(ratio);
        }

        @Override
        public void onZoomStart() {
        }

        @Override
        public void onZoomEnd() {
        }
    };

    public BlurModuleUI(CameraActivity activity, View parent, BlurModuleUIListener listener) {
        mActivity = activity;
        mListener = listener;
        mRootView = parent;
        initAlphaAnimation();

        ViewGroup moduleRoot = (ViewGroup) mRootView.findViewById(R.id.module_layout);
        mActivity.getLayoutInflater().inflate(R.layout.blur_layout, moduleRoot, true);

        mPreviewView = (TextureView) mRootView.findViewById(R.id.preview_content);

        mPreviewOverlay = (PreviewOverlay) mRootView.findViewById(R.id.preview_overlay);
        mProgressOverlay = (ProgressOverlay) mRootView.findViewById(R.id.progress_overlay);

        mFocusRing = (FocusRing) mRootView.findViewById(R.id.focus_ring);
        mCountdownView = (CountDownView) mRootView.findViewById(R.id.count_down_view);

//        mAppendSetting = (AppendSettingLayout) mRootView.findViewById(R.id.module_append_setting);
        mBlurIndicatorView = (BlurIndicatorView) mRootView.findViewById(R.id.blur_indicator_view);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.blur_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarListener());
    }

    /**
     * Getter for the width of the visible area of the preview.
     */
    public int getPreviewAreaWidth() {
        return mPreviewAreaWidth;
    }

    /**
     * Getter for the height of the visible area of the preview.
     */
    public int getPreviewAreaHeight() {
        return mPreviewAreaHeight;
    }

    public Matrix getPreviewTransform(Matrix m) {
        return mPreviewView.getTransform(m);
    }

    public FocusRing getFocusRing() {
        return mFocusRing;
    }

    public void showDebugMessage(String message) {
        /* NoOp */
    }

    /**
     * Starts the countdown timer.
     *
     * @param sec seconds to countdown
     */
    public void startCountdown(int sec) {
        mCountdownView.startCountDown(sec);
    }

    /**
     * Sets a listener that gets notified when the countdown is finished.
     */
    public void setCountdownFinishedListener(CountDownView.OnCountDownStatusListener listener) {
        mCountdownView.setCountDownStatusListener(listener);
    }

    /**
     * Returns whether the countdown is on-going.
     */
    public boolean isCountingDown() {
        return mCountdownView.isCountingDown();
    }

    /**
     * Cancels the on-going countdown, if any.
     */
    public void cancelCountDown() {
        mCountdownView.cancelCountDown();
    }

    /**
     * Sets the progress of the gcam picture taking.
     *
     * @param percent amount of process done in percent 0-100.
     */
    public void setPictureTakingProgress(int percent) {
        mProgressOverlay.setProgress(percent);
    }

    public Bitmap getBitMapFromPreview() {
        Matrix m = new Matrix();
        m = getPreviewTransform(m);
        Bitmap src = mPreviewView.getBitmap();
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
    }

    /**
     * Enables zoom UI, setting maximum zoom.
     * Called from Module when camera is available.
     *
     * @param maxZoom maximum zoom value.
     */
    public void initializeZoom(float maxZoom) {
        mMaxZoom = maxZoom;
        mPreviewOverlay.setupZoom(mMaxZoom, 0, mZoomChangedListener);
    }

    @Override
    public void onPreviewAreaChanged(RectF previewArea) {
        // TODO: mFaceView.onPreviewAreaChanged(previewArea);
        mCountdownView.onPreviewAreaChanged(previewArea);
        mProgressOverlay.setBounds(previewArea);
        updateSeekBarPosition(previewArea);
        updateBlurIndicatorPosition(previewArea);
    }

    private void updateSeekBarPosition(RectF area){
        if(mCaptureLayoutHelper.shouldOverlayBottomBar()){
            if (area.width() > 0 && area.height() > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSeekBar.getLayoutParams();
                params.setMargins((int) area.left, (int) area.height()-(int) mCaptureLayoutHelper.getBottomBarRect().height(), 0, 0);
                mSeekBar.setLayoutParams(params);
            }
        }else{
            if (area.width() > 0 && area.height() > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSeekBar.getLayoutParams();
                params.setMargins((int) area.left, (int) area.height(), 0, 0);
                mSeekBar.setLayoutParams(params);
            }
        }
    }

    private void updateBlurIndicatorPosition(RectF area){
        if(mCaptureLayoutHelper.shouldOverlayBottomBar()){
            if (area.width() > 0 && area.height() > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mBlurIndicatorView.getLayoutParams();
                params.width = (int) area.width();
                params.height= (int) area.height();
                params.setMargins((int) area.left, (int) area.top, 0, 0);
                mBlurIndicatorView.setLayoutParams(params);
            }
        }else{
            if (area.width() > 0 && area.height() > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mBlurIndicatorView.getLayoutParams();
                params.width = (int) area.width();
                params.height= (int) area.height();
                params.setMargins((int) area.left, (int) area.top, 0, 0);
                mBlurIndicatorView.setLayoutParams(params);
            }
        }
    }

    public void updateBlurIndicatorViewCenter(float x,float y){
        if (mBlurIndicatorView != null){
            mBlurIndicatorView.setCenter(x,y);
        }

    }

    public void setCaptureLayoutHelper(CaptureLayoutHelper captureLayoutHelper){
        mCaptureLayoutHelper = captureLayoutHelper;
    }

    private class OnSeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float radius = (progress-50)*3+200;
            mBlurIndicatorView.setRadius(radius);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mAlpha.cancel();
            mBlurIndicatorView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mAlpha.setTarget(mBlurIndicatorView);
            mAlpha.start();
        }
    }

    public void updateBlurIndicatorViewVisibility(int flag){
        if(mBlurIndicatorView != null){
            if(flag == View.INVISIBLE){
                mAlpha.setTarget(mBlurIndicatorView);
                mAlpha.start();
            }else{
                mBlurIndicatorView.setVisibility(flag);
            }
        }
    }

    private ObjectAnimator mAlpha = ObjectAnimator.ofFloat(null,"alpha",1.0f,0.0f);
    private void initAlphaAnimation(){
        mAlpha.setDuration(2000);
        mAlpha.setTarget(mBlurIndicatorView);
        mAlpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBlurIndicatorView.setVisibility(View.INVISIBLE);
                mBlurIndicatorView.setAlpha(1.0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mBlurIndicatorView.setVisibility(View.INVISIBLE);
                mBlurIndicatorView.setAlpha(1.0f);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAlpha.start();
    }

    public void removeAlphaAnimation(){
        mBlurIndicatorView.clearAnimation();
        mAlpha.cancel();
    }

    public float getCurrentRadius(){
        return mBlurIndicatorView.getCurrentRadius();
    }
}

