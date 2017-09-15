package com.appends.JniUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.android.camera.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class EffectUtil {
    static {
        System.loadLibrary("native-lib");
    }
    public static native String stringFromJNI();
    public static native int[] getResultFromJni(int[] pixels,int width,int height,int centerX,int centerY,int radius);

    private Context mContext;
    private Bitmap mOriginalBitmap;
    private Bitmap mBlurBitmap;
    private int mWidth,mHeight;
    private String mPath;
    private String mTitle;

    public EffectUtil(Context context,byte[] bytes,int inSimple,String title,String path,String mimeType){
        mContext = context;
        if(bytes != null && bytes.length>0){
            int simpleSize = (int) Math.pow(2,inSimple);
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inSampleSize = simpleSize;
            mOriginalBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,ops);
            mWidth = mOriginalBitmap.getWidth();
            mHeight = mOriginalBitmap.getHeight();
            mTitle = title+"_brokeh";
            mPath = Storage.generateFilepath(path,mTitle,mimeType);
        }
        initRenderScript();
    }

    private void initRenderScript(){
        mBlurBitmap = Bitmap.createBitmap(mOriginalBitmap.getWidth(),mOriginalBitmap.getHeight(),mOriginalBitmap.getConfig());
        RenderScript rs = RenderScript.create(mContext);
        Allocation in = Allocation.createFromBitmap(rs,mOriginalBitmap);
        Allocation out = Allocation.createFromBitmap(rs,mBlurBitmap);
        ScriptIntrinsicBlur mBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        mBlur.setInput(in);
        mBlur.setRadius(25);
        mBlur.forEach(out);
        out.copyTo(mBlurBitmap);
        in.destroy();
        mBlur.destroy();
        out.destroy();
        rs.destroy();
    }

    public void dealWithBitmap(int centerX,int centerY,int radius){
        int[] originalData = new int[mWidth*mHeight];
        mBlurBitmap.getPixels(originalData,0,mWidth,0,0,
                mWidth,mHeight);
        int[] resultData = getResultFromJni(originalData,mWidth,mHeight,centerX,centerY,radius);
        mBlurBitmap.setPixels(resultData,0,mWidth,0,0, mWidth,mHeight);
        Bitmap finalBitmap = Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(finalBitmap);
        c.drawBitmap(mOriginalBitmap,0,0,null);
        int flag = c.save();
        c.drawBitmap(mBlurBitmap,0,0,null);
        c.restoreToCount(flag);
        File file = new File(mPath);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();
        }catch (IOException e){
            android.util.Log.e("zyq","IOException : e = "+e.getMessage());
        }
        notifySystemGallery(file);
    }

    private void notifySystemGallery(File file){
        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),file.getAbsolutePath(),file.getName(),null);
        }catch (FileNotFoundException e){
            android.util.Log.e("zyq","IOException : e = "+e.getMessage());
        }

        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+mPath)));
    }
}
