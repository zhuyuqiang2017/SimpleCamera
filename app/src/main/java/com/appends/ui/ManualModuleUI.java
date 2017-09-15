package com.appends.ui;

/**
 * Created by Administrator on 2017/9/3 0003.
 */

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.CaptureLayoutHelper;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.CountDownView;
import com.android.camera.ui.PreviewOverlay;
import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.ProgressOverlay;
import com.android.camera.ui.focus.FocusRing;
import com.android.camera2.R;
import com.appends.common.DetailSettingItem;

import java.util.ArrayList;

/**
 * Contains the UI for the CaptureModule.
 */
public class ManualModuleUI implements PreviewStatusListener.PreviewAreaChangedListener , View.OnClickListener{

    public interface ManualModuleUIListener {
        public void onZoomRatioChanged(float zoomRatio);
    }

    private static final Log.Tag TAG = new Log.Tag("ManualModuleUI");

    private final CameraActivity mActivity;
    private final ManualModuleUIListener mListener;
    private final View mRootView;

    private final PreviewOverlay mPreviewOverlay;
    private final ProgressOverlay mProgressOverlay;
    private final TextureView mPreviewView;

    private final FocusRing mFocusRing;
    private final CountDownView mCountdownView;

    private int mPreviewAreaWidth;
    private int mPreviewAreaHeight;

    /** Maximum zoom; intialize to 1.0 (disabled) */
    private float mMaxZoom = 1f;

    private SettingsManager mSettingsManager;
    private AppendSettingLayout mManualSettingContainer;
    private CaptureLayoutHelper mCaptureLayoutHelper;
    private ManualSettingGroup mManualSettingGroup;
    private ManualSettingGroup mManualAwbSettings;
    private ManualSettingGroup mManualEffectSettings;
    private ManualSettingGroup mManualSceneSettings;
    private ManualSettingGroup mManualShadingSettings;
    private ManualSettingGroup mManualEdgeSettings;
    private ManualSettingGroup mManualExposureSettings;
    private FrameLayout mManualDetailSettingLayout;
    private TextView mManualTitle;
    private ArrayList<DetailSettingItem> mManualSettingItems;
    private ArrayList<DetailSettingItem> mManualAwbSettingItems;
    private ArrayList<DetailSettingItem> mManualEffectSettingItems;
    private ArrayList<DetailSettingItem> mManualSceneSettingItems;
    private ArrayList<DetailSettingItem> mManualShadingSettingItems;
    private ArrayList<DetailSettingItem> mManualEdgeSettingItems;
    private ArrayList<DetailSettingItem> mManualExposureSettingItems;
    private float density = 1.0f;
    private String mPrefix = "";
    private int mDetailTextGravity = Gravity.CENTER;
    private int mDetailTextSize = 8;
    private int mMinExposure = -9;
    private int mExposureSetup = 3;

    public interface OnAwbChangeListener{
        void onAwbChanged(int value);
    }
    private OnAwbChangeListener mAwbChangeListener;
    public void setOnAwbChangeListener(OnAwbChangeListener listener){
        mAwbChangeListener = listener;
    }

    public interface OnEffectChangeListener{
        void onEffectChanged(int value);
    }
    private OnEffectChangeListener mEffectChangeListener;
    public void setOnEffectChangeListener(OnEffectChangeListener listener){
        mEffectChangeListener = listener;
    }

    public interface OnSceneChangeListener{
        void onSceneChanged(int value);
    }
    private OnSceneChangeListener mSceneChangeListener;
    public void setOnSceneChangeListener(OnSceneChangeListener listener){
        mSceneChangeListener = listener;
    }

    public interface OnShadingChangeListener{
        void onShadingChanged(int value);
    }
    private OnShadingChangeListener mShadingChangeListener;
    public void setOnShadingChangeListener(OnShadingChangeListener listener){
        mShadingChangeListener = listener;
    }

    public interface OnEdgeChangeListener{
        void onEdgeChanged(int value);
    }
    private OnEdgeChangeListener mEdgeChangeListener;
    public void setOnEdgeChangeListener(OnEdgeChangeListener listener){
        mEdgeChangeListener = listener;
    }

    public interface OnExposureChangeListener{
        void onExposureChanged(int value);
    }
    private OnExposureChangeListener mExposureChangeListener;
    public void setOnExposureChangeListener(OnExposureChangeListener listener){
        mExposureChangeListener = listener;
    }


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

    public ManualModuleUI(CameraActivity activity, View parent, ManualModuleUIListener listener, SettingsManager settingsManager) {
        mActivity = activity;
        mListener = listener;
        mRootView = parent;
        mSettingsManager = settingsManager;

        ViewGroup moduleRoot = (ViewGroup) mRootView.findViewById(R.id.module_layout);
        mActivity.getLayoutInflater().inflate(R.layout.manual_layout, moduleRoot, true);

        mPreviewView = (TextureView) mRootView.findViewById(R.id.preview_content);

        mPreviewOverlay = (PreviewOverlay) mRootView.findViewById(R.id.preview_overlay);
        mProgressOverlay = (ProgressOverlay) mRootView.findViewById(R.id.progress_overlay);

        mFocusRing = (FocusRing) mRootView.findViewById(R.id.focus_ring);
        mCountdownView = (CountDownView) mRootView.findViewById(R.id.count_down_view);
        mManualSettingContainer = (AppendSettingLayout) mRootView.findViewById(R.id.manual_setting_layout);
        mManualSettingGroup = (ManualSettingGroup) mRootView.findViewById(R.id.manual_setting_group);
        mManualDetailSettingLayout = (FrameLayout) mRootView.findViewById(R.id.manual_detail_setting_group);
        mManualTitle = (TextView) mRootView.findViewById(R.id.manual_title);
        initManualModuleUI();
    }

    private void initManualModuleUI(){
        density = mActivity.getResources().getDisplayMetrics().density;
        mDetailTextSize = (int) (8*density);
        initManualSettingLayout();
        initManualAwbSettingLayout();
        initManualEffectSettingLayout();
        initManualSceneSettingLayout();
        initManualShadingSettingLayout();
        initManualEdgeSettingLayout();
    }

    private void initManualSettingLayout(){
        String[] supportSettings = mActivity.getResources().getStringArray(R.array.camera_support_setting_array);
        TypedArray mNormalDrawalbes = mActivity.getResources().obtainTypedArray(R.array.camera_support_setting_drawable_normal);
        TypedArray mHighLightDrawalbes = mActivity.getResources().obtainTypedArray(R.array.camera_support_setting_drawable_highlight);
        if (supportSettings.length != mNormalDrawalbes.length() || supportSettings.length != mHighLightDrawalbes.length()
                || mNormalDrawalbes.length() != mHighLightDrawalbes.length()){
            throw new IllegalArgumentException("array length is wrong!!!");
        }

        mManualSettingItems = new ArrayList<>();
        for (int i = 0 ; i < supportSettings.length ; i ++){
            DetailSettingItem item = new DetailSettingItem(supportSettings[i]);
            item.setHighLightDrawable(mActivity.getDrawable(mHighLightDrawalbes.getResourceId(i,0)));
            item.setNormalDrawable(mActivity.getDrawable(mNormalDrawalbes.getResourceId(i,0)));
            item.setValue(i);
            mManualSettingItems.add(i,item);
        }
        initItemViewForSettingLayout();
    }

    private void initItemViewForSettingLayout(){
        if (mManualSettingGroup != null && mManualSettingItems.size() > 0){
            for (int i = 0; i < mManualSettingItems.size() ; i ++){
                DetailSettingItem item = mManualSettingItems.get(i);
                TextView itemView = new TextView(mActivity);
                itemView.setTextColor(item.getNormalColor());
                itemView.setText(item.getName());
                Drawable drawable = item.getNormalDrawable();
                drawable.setBounds(0,0,(int)(24*density),(int)(24*density));
                itemView.setCompoundDrawables(null,drawable,null,null);
                itemView.setPadding((int)(20*density),0,(int)(20*density),0);
                itemView.setOnClickListener(this);
                itemView.setTag(item.getName());
                mManualSettingGroup.addView(itemView,i);
            }
        }
    }

    private void initManualAwbSettingLayout(){
        String[] awbSettingNames = mActivity.getResources().getStringArray(R.array.camera_setting_awb_entry);
        int[] awbSettingValue = mActivity.getResources().getIntArray(R.array.camera_setting_awb_entryValues);
        if(awbSettingNames.length != awbSettingValue.length){
            throw new IllegalArgumentException("array length is wrong!!!");
        }
        mManualAwbSettingItems = new ArrayList<>();
        for (int i = 0; i < awbSettingNames.length ; i ++){
            DetailSettingItem item = new DetailSettingItem(awbSettingNames[i]);
            item.setValue(awbSettingValue[i]);
            mManualAwbSettingItems.add(i,item);
        }
        mManualAwbSettings = new ManualSettingGroup(mActivity);
        for (int i = 0; i < mManualAwbSettingItems.size() ; i ++){
            TextView view = new TextView(mActivity);
            view.setPadding((int)(15*density),0,(int)(15*density),0);
            view.setText(mManualAwbSettingItems.get(i).getName());
            view.setTextColor(mManualAwbSettingItems.get(i).getNormalColor());
            view.setOnClickListener(new onAwbClickListener());
            view.setTag(mManualAwbSettingItems.get(i).getValue());
            view.setGravity(mDetailTextGravity);
            view.setTextSize(mDetailTextSize);
            mManualAwbSettings.addView(view,i,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void initManualEffectSettingLayout(){
        String[] effectSettingNames = mActivity.getResources().getStringArray(R.array.camera_setting_effect_entry);
        int[] effectSettingValue = mActivity.getResources().getIntArray(R.array.camera_setting_effect_entryValue);
        if(effectSettingNames.length != effectSettingValue.length){
            throw new IllegalArgumentException("array length is wrong!!!");
        }
        mManualEffectSettingItems = new ArrayList<>();
        for (int i = 0; i < effectSettingNames.length ; i ++){
            DetailSettingItem item = new DetailSettingItem(effectSettingNames[i]);
            item.setValue(effectSettingValue[i]);
            mManualEffectSettingItems.add(i,item);
        }
        mManualEffectSettings = new ManualSettingGroup(mActivity);
        for (int i = 0; i < mManualEffectSettingItems.size() ; i ++){
            TextView view = new TextView(mActivity);
            view.setPadding((int)(15*density),0,(int)(15*density),0);
            view.setText(mManualEffectSettingItems.get(i).getName());
            view.setTextColor(mManualEffectSettingItems.get(i).getNormalColor());
            view.setOnClickListener(new onEffectClickListener());
            view.setTag(mManualEffectSettingItems.get(i).getValue());
            view.setGravity(mDetailTextGravity);
            view.setTextSize(mDetailTextSize);
            mManualEffectSettings.addView(view,i,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void initManualSceneSettingLayout(){
        String[] sceneSettingNames = mActivity.getResources().getStringArray(R.array.camera_setting_scene_entry);
        int[] sceneSettingValue = mActivity.getResources().getIntArray(R.array.camera_setting_scene_entryValue);
        if(sceneSettingNames.length != sceneSettingValue.length){
            throw new IllegalArgumentException("array length is wrong!!!");
        }
        mManualSceneSettingItems = new ArrayList<>();
        for (int i = 0; i < sceneSettingNames.length ; i ++){
            DetailSettingItem item = new DetailSettingItem(sceneSettingNames[i]);
            item.setValue(sceneSettingValue[i]);
            mManualSceneSettingItems.add(i,item);
        }
        mManualSceneSettings = new ManualSettingGroup(mActivity);
        for (int i = 0; i < mManualSceneSettingItems.size() ; i ++){
            TextView view = new TextView(mActivity);
            view.setPadding((int)(15*density),0,(int)(15*density),0);
            view.setText(mManualSceneSettingItems.get(i).getName());
            view.setTextColor(mManualSceneSettingItems.get(i).getNormalColor());
            view.setOnClickListener(new onSceneClickListener());
            view.setTag(mManualSceneSettingItems.get(i).getValue());
            view.setGravity(mDetailTextGravity);
            view.setTextSize(mDetailTextSize);
            mManualSceneSettings.addView(view,i,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void initManualShadingSettingLayout(){
        String[] shadingSettingNames = mActivity.getResources().getStringArray(R.array.camera_setting_shading_entry);
        int[] shadingSettingValue = mActivity.getResources().getIntArray(R.array.camera_setting_shading_entryValue);
        if(shadingSettingNames.length != shadingSettingValue.length){
            throw new IllegalArgumentException("array length is wrong!!!");
        }
        mManualShadingSettingItems = new ArrayList<>();
        for (int i = 0; i < shadingSettingNames.length ; i ++){
            DetailSettingItem item = new DetailSettingItem(shadingSettingNames[i]);
            item.setValue(shadingSettingValue[i]);
            mManualShadingSettingItems.add(i,item);
        }
        mManualShadingSettings = new ManualSettingGroup(mActivity);
        for (int i = 0; i < mManualShadingSettingItems.size() ; i ++){
            TextView view = new TextView(mActivity);
            view.setPadding((int)(15*density),0,(int)(15*density),0);
            view.setText(mManualShadingSettingItems.get(i).getName());
            view.setTextColor(mManualShadingSettingItems.get(i).getNormalColor());
            view.setOnClickListener(new onShadingClickListener());
            view.setTag(mManualShadingSettingItems.get(i).getValue());
            view.setGravity(mDetailTextGravity);
            view.setTextSize(mDetailTextSize);
            mManualShadingSettings.addView(view,i,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void initManualEdgeSettingLayout(){
        String[] edgeSettingNames = mActivity.getResources().getStringArray(R.array.camera_setting_edge_entry);
        int[] edgeSettingValue = mActivity.getResources().getIntArray(R.array.camera_setting_edge_entryValue);
        if(edgeSettingNames.length != edgeSettingValue.length){
            throw new IllegalArgumentException("array length is wrong!!!");
        }
        mManualEdgeSettingItems = new ArrayList<>();
        for (int i = 0; i < edgeSettingNames.length ; i ++){
            DetailSettingItem item = new DetailSettingItem(edgeSettingNames[i]);
            item.setValue(edgeSettingValue[i]);
            mManualEdgeSettingItems.add(i,item);
        }
        mManualEdgeSettings = new ManualSettingGroup(mActivity);
        for (int i = 0; i < mManualEdgeSettingItems.size() ; i ++){
            TextView view = new TextView(mActivity);
            view.setPadding((int)(15*density),0,(int)(15*density),0);
            view.setText(mManualEdgeSettingItems.get(i).getName());
            view.setTextColor(mManualEdgeSettingItems.get(i).getNormalColor());
            view.setOnClickListener(new onEdgeClickListener());
            view.setTag(mManualEdgeSettingItems.get(i).getValue());
            view.setGravity(mDetailTextGravity);
            view.setTextSize(mDetailTextSize);
            mManualEdgeSettings.addView(view,i,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public void setExposureSetting(ArrayList<DetailSettingItem> exposureSettingItems,int min,int setup) {
        mMinExposure = min;
        mExposureSetup = setup;
        mManualExposureSettingItems = exposureSettingItems;
        mManualExposureSettings = new ManualSettingGroup(mActivity);
        for (int i = 0; i < mManualExposureSettingItems.size() ; i ++){
            TextView view = new TextView(mActivity);
            view.setPadding((int)(15*density),0,(int)(15*density),0);
            view.setText(mManualExposureSettingItems.get(i).getName());
            view.setTextColor(mManualExposureSettingItems.get(i).getNormalColor());
            view.setOnClickListener(new onExposureClickListener());
            view.setTag(mManualExposureSettingItems.get(i).getValue());
            view.setGravity(mDetailTextGravity);
            view.setTextSize(mDetailTextSize);
            mManualExposureSettings.addView(view,i,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof TextView)){
            return ;
        }
        mManualDetailSettingLayout.removeAllViews();
        String tag = (String) v.getTag();
        int index = 0;
        android.util.Log.i("zyq","tag = "+tag);
        if ("awb".equals(tag)){
            mPrefix = "Awb : ";
            index = 0;
            initAndDisplayAwbSetting();
        }else if ("exposure".equals(tag)){
            mPrefix = "Exposure : ";
            index = 1;
            initAndDisplayExposureSetting();
        }else if ("effect".equals(tag)){
            mPrefix = "Effect : ";
            index = 2;
            initAndDisplayEffectSetting();
        }else if ("scene".equals(tag)){
            mPrefix = "Scene : ";
            index = 3;
            initAndDisplaySceneSetting();
        }else if ("shading".equals(tag)){
            mPrefix = "Shading : ";
            index = 4;
            initAndDisplayShadingSetting();
        }else if ("edge".equals(tag)){
            mPrefix = "Edge : ";
            index = 5;
            initAndDisplayEdgeSetting();
        }
        for (int i = 0; i < mManualSettingItems.size(); i ++){
            TextView view = (TextView) mManualSettingGroup.getChildAt(i);
            view.setTextColor(mManualSettingItems.get(i).getNormalColor());
            view.setCompoundDrawables(null,mManualSettingItems.get(i).getNormalDrawable(),null,null);
            mManualSettingItems.get(i).setIsSelected(false);
        }
        TextView view = (TextView) mManualSettingGroup.getChildAt(index);
        view.setTextColor(mManualSettingItems.get(index).getHighLightColor());
        Drawable drawable = mManualSettingItems.get(index).getHighLightDrawable();
        drawable.setBounds(0,0,(int)(24*density),(int)(24*density));
        view.setCompoundDrawables(null,drawable,null,null);
        mManualSettingItems.get(index).setIsSelected(true);

    }

    private void initAndDisplayAwbSetting(){
        mManualDetailSettingLayout.addView(mManualAwbSettings,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        int awbValue = mSettingsManager.getInteger(mActivity.getCameraScope(), Keys.KEY_CAMERA_AWB_MODE);
        mManualTitle.setText(mPrefix+mManualAwbSettingItems.get(awbValue).getName());
        TextView view = (TextView) mManualAwbSettings.getChildAt(awbValue);
        view.setTextColor(mManualAwbSettingItems.get(awbValue).getHighLightColor());
        mManualAwbSettingItems.get(awbValue).setIsSelected(true);
    }

    private void initAndDisplayEffectSetting() {
        mManualDetailSettingLayout.addView(mManualEffectSettings,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        int effectValue = mSettingsManager.getInteger(mActivity.getCameraScope(), Keys.KEY_CAMERA_EFFECT_MODE);
        mManualTitle.setText(mPrefix+mManualEffectSettingItems.get(effectValue).getName());
        TextView view = (TextView) mManualEffectSettings.getChildAt(effectValue);
        view.setTextColor(mManualEffectSettingItems.get(effectValue).getHighLightColor());
        mManualEffectSettingItems.get(effectValue).setIsSelected(true);
    }

    private void initAndDisplaySceneSetting() {
        mManualDetailSettingLayout.addView(mManualSceneSettings,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        int effectValue = mSettingsManager.getInteger(mActivity.getCameraScope(), Keys.KEY_CAMERA_SCENE_MODE);
        mManualTitle.setText(mPrefix+mManualSceneSettingItems.get(effectValue).getName());
        TextView view = (TextView) mManualSceneSettings.getChildAt(effectValue);
        view.setTextColor(mManualSceneSettingItems.get(effectValue).getHighLightColor());
        mManualSceneSettingItems.get(effectValue).setIsSelected(true);
    }

    private void initAndDisplayShadingSetting() {
        mManualDetailSettingLayout.addView(mManualShadingSettings,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        int effectValue = mSettingsManager.getInteger(mActivity.getCameraScope(), Keys.KEY_CAMERA_SHADING);
        mManualTitle.setText(mPrefix+mManualShadingSettingItems.get(effectValue).getName());
        TextView view = (TextView) mManualShadingSettings.getChildAt(effectValue);
        view.setTextColor(mManualShadingSettingItems.get(effectValue).getHighLightColor());
        mManualShadingSettingItems.get(effectValue).setIsSelected(true);
    }

    private void initAndDisplayEdgeSetting() {
        mManualDetailSettingLayout.addView(mManualEdgeSettings,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        int effectValue = mSettingsManager.getInteger(mActivity.getCameraScope(), Keys.KEY_CAMERA_EDGE);
        mManualTitle.setText(mPrefix+mManualEdgeSettingItems.get(effectValue).getName());
        TextView view = (TextView) mManualEdgeSettings.getChildAt(effectValue);
        view.setTextColor(mManualEdgeSettingItems.get(effectValue).getHighLightColor());
        mManualEdgeSettingItems.get(effectValue).setIsSelected(true);
    }

    private void initAndDisplayExposureSetting() {
        mManualDetailSettingLayout.addView(mManualExposureSettings,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        int effectValue = mSettingsManager.getInteger(mActivity.getCameraScope(), Keys.KEY_EXPOSURE);

        for (int i = 0 ; i < mManualExposureSettingItems.size() ; i ++){
            TextView view = (TextView) mManualExposureSettings.getChildAt(i);
            view.setTextColor(mManualExposureSettingItems.get(i).getNormalColor());
            mManualExposureSettingItems.get(i).setIsSelected(false);
        }

        int index = effectValue-mManualExposureSettingItems.get(0).getValue();
        android.util.Log.e("zyq","effect value = "+effectValue + " , index = "+index);
        mManualTitle.setText(mPrefix+mManualExposureSettingItems.get(index).getName());
        TextView view = (TextView) mManualExposureSettings.getChildAt(index);
        view.setTextColor(mManualExposureSettingItems.get(index).getHighLightColor());
        mManualExposureSettingItems.get(index).setIsSelected(true);
    }

    private class onAwbClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer){
                int index = (int) v.getTag();
                android.util.Log.i("zyq","index = "+index);
                for (int i = 0 ; i < mManualAwbSettingItems.size() ; i ++){
                    TextView view = (TextView) mManualAwbSettings.getChildAt(i);
                    view.setTextColor(mManualAwbSettingItems.get(i).getNormalColor());
                    mManualAwbSettingItems.get(i).setIsSelected(false);
                }
                ((TextView)v).setTextColor(mManualAwbSettingItems.get(index).getHighLightColor());
                mManualAwbSettingItems.get(index).setIsSelected(true);
                if(mAwbChangeListener != null){
                    mAwbChangeListener.onAwbChanged(index);
                }
                mManualTitle.setText(mPrefix+mManualAwbSettingItems.get(index).getName());
            }
        }
    }

    private class onEffectClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer){
                int index = (int) v.getTag();
                android.util.Log.i("zyq","index = "+index);
                for (int i = 0 ; i < mManualEffectSettingItems.size() ; i ++){
                    TextView view = (TextView) mManualEffectSettings.getChildAt(i);
                    view.setTextColor(mManualEffectSettingItems.get(i).getNormalColor());
                    mManualEffectSettingItems.get(i).setIsSelected(false);
                }
                ((TextView)v).setTextColor(mManualEffectSettingItems.get(index).getHighLightColor());
                mManualEffectSettingItems.get(index).setIsSelected(true);
                if(mEffectChangeListener != null){
                    mEffectChangeListener.onEffectChanged(index);
                }
                mManualTitle.setText(mPrefix+mManualEffectSettingItems.get(index).getName());
            }
        }
    }

    private class onSceneClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer){
                int index = (int) v.getTag();
                android.util.Log.i("zyq","index = "+index);
                for (int i = 0 ; i < mManualSceneSettingItems.size() ; i ++){
                    TextView view = (TextView) mManualSceneSettings.getChildAt(i);
                    view.setTextColor(mManualSceneSettingItems.get(i).getNormalColor());
                    mManualSceneSettingItems.get(i).setIsSelected(false);
                }
                ((TextView)v).setTextColor(mManualSceneSettingItems.get(index).getHighLightColor());
                mManualSceneSettingItems.get(index).setIsSelected(true);
                if(mSceneChangeListener != null){
                    mSceneChangeListener.onSceneChanged(index);
                }
                mManualTitle.setText(mPrefix+mManualSceneSettingItems.get(index).getName());
            }
        }
    }

    private class onShadingClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer){
                int index = (int) v.getTag();
                android.util.Log.i("zyq","index = "+index);
                for (int i = 0 ; i < mManualShadingSettingItems.size() ; i ++){
                    TextView view = (TextView) mManualShadingSettings.getChildAt(i);
                    view.setTextColor(mManualShadingSettingItems.get(i).getNormalColor());
                    mManualShadingSettingItems.get(i).setIsSelected(false);
                }
                ((TextView)v).setTextColor(mManualShadingSettingItems.get(index).getHighLightColor());
                mManualShadingSettingItems.get(index).setIsSelected(true);
                if(mShadingChangeListener != null){
                    mShadingChangeListener.onShadingChanged(index);
                }
                mManualTitle.setText(mPrefix+mManualShadingSettingItems.get(index).getName());
            }
        }
    }

    private class onEdgeClickListener implements View.OnClickListener{
            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof Integer){
                    int index = (int) v.getTag();
                    android.util.Log.i("zyq","index = "+index);
                    for (int i = 0 ; i < mManualEdgeSettingItems.size() ; i ++){
                        TextView view = (TextView) mManualEdgeSettings.getChildAt(i);
                        view.setTextColor(mManualEdgeSettingItems.get(i).getNormalColor());
                        mManualEdgeSettingItems.get(i).setIsSelected(false);
                    }
                    ((TextView)v).setTextColor(mManualEdgeSettingItems.get(index).getHighLightColor());
                    mManualEdgeSettingItems.get(index).setIsSelected(true);
                    if(mEdgeChangeListener != null){
                        mEdgeChangeListener.onEdgeChanged(index);
                    }
                    mManualTitle.setText(mPrefix+mManualEdgeSettingItems.get(index).getName());
                }
            }
        }

    private class onExposureClickListener implements View.OnClickListener{
            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof Integer){
                    int value = (int) v.getTag();
                    int index = (value-mMinExposure)/mExposureSetup;
                    android.util.Log.i("zyq","index = "+index);
                    for (int i = 0 ; i < mManualExposureSettingItems.size() ; i ++){
                        TextView view = (TextView) mManualExposureSettings.getChildAt(i);
                        view.setTextColor(mManualExposureSettingItems.get(i).getNormalColor());
                        mManualExposureSettingItems.get(i).setIsSelected(false);
                    }
                    ((TextView)v).setTextColor(mManualExposureSettingItems.get(index).getHighLightColor());
                    mManualExposureSettingItems.get(index).setIsSelected(true);
                    if(mExposureChangeListener != null){
                        mExposureChangeListener.onExposureChanged(value);
                    }
                    mManualTitle.setText(mPrefix+mManualExposureSettingItems.get(index).getName());
                }
            }
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
        mManualSettingContainer.setBounds(previewArea,mCaptureLayoutHelper);
    }

    public void setCaptureLayoutHelper(CaptureLayoutHelper captureLayoutHelper){
        mCaptureLayoutHelper = captureLayoutHelper;
    }
}

