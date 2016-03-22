package com.fjwangjia.android.seele;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by flb on 16/3/22.
 */
public class BugFix {

    /**
     * 基础路径
     */
    static String BASE_URL = "";
    /**
     * 补丁的存储路径  app文件路径 + "PATCH_DIR/" + mCurrentVersion
     */
    static String PATCH_DIR = "";
    static String PERSISTENCE_NAME = "/SEELE_BUG_FIX";
    static String LAST_VERSION = "LAST_VERSION";
    static Context mContext = null;
    static int mCurrentVersion;
    static Verify mVerify = null;
    static PatchLoader mPatchLoader = null;
    public static void init(Context context){
        mContext = context;

        BASE_URL = context.getFilesDir() + "PATCH_DIR/";
        PATCH_DIR = BASE_URL + mCurrentVersion;

        mVerify = new Verify(context);
        mPatchLoader = new PatchLoader();
        mCurrentVersion = 0;
        try
        {
            String pkName = context.getPackageName();
            mCurrentVersion = context.getPackageManager().getPackageInfo(pkName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        int iRecordVersion = getRecordVersion(context);

        /*
        记录版本号小于当前版本号 清理所有补丁
         */
        if (iRecordVersion<mCurrentVersion){
            cleanPatch();
        }

        /*
         *不同版本放在不同的文件夹下
         */
        File file = new File(PATCH_DIR);
        if(!file.exists()){
            file.mkdirs();
        }else {
            loadPatch();
        }
        
        
    }

    public static int getRecordVersion(Context context){
        if (null != context) {
            SharedPreferences sp = context.getSharedPreferences(PERSISTENCE_NAME, 0);
            return sp.getInt(LAST_VERSION, -1);
        }
        return 0;
    }

    public static void setRecordVersion(Context context,int version){
        SharedPreferences sp = context.getSharedPreferences(PERSISTENCE_NAME, 0);
        sp.edit().putInt(LAST_VERSION, version).commit();
    }

    /**
     * 加载所有路径下所有的补丁包
     */
    private static void loadPatch(){
        File patchDir = new File(PATCH_DIR);

        ArrayList<File> patchList = new ArrayList<>();
        for (File patch : patchDir.listFiles()){
            if(patch.isFile()&&mVerify.verify(patch)){
                patchList.add(patch);
            }else {
                Log.e("seele_bug_fix","verify fail or is dir"+patch.getAbsolutePath());
            }
        }

        mPatchLoader.load((File[]) patchList.toArray());
    }

    
    public static void loadPatch(File src){
        File dest = new File(PATCH_DIR, src.getName());
        if(!src.exists()){
            Log.e("seele_bug_fix","there is not exists file : "+src.getAbsolutePath());
            return;
        }
        if (dest.exists()) {
            return;
        }
        if(mVerify.verify(src)){
            Log.e("seele_bug_fix","verify fail"+src.getAbsolutePath());
        }

        try {
            copyFile(src,dest);
            mPatchLoader.load(dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    public static void loadPatch(String path){
        File src = new File(path);
        loadPatch(src);
    }

    public static void cleanPatch(){
        if(mContext != null){
            File baseDir = new File(BASE_URL);
            baseDir.delete();
        }
    }
}
