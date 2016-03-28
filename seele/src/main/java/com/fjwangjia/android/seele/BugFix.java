package com.fjwangjia.android.seele;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    static String PATCH_FILE = "apk.patch";
    static String DEX_OPT_DIR = "OPT_DIR";
    static String PERSISTENCE_NAME = "SEELE_BUG_FIX";
    static String LAST_VERSION = "LAST_VERSION";
    static Context mContext = null;
    static int mCurrentVersion;
    static Verify mVerify = null;
    static PatchLoader mPatchLoader = null;



    public static void init(Context context){
        mContext = context;
        BASE_URL = context.getFilesDir() + "PATCH_DIR/";
        PATCH_DIR = BASE_URL + mCurrentVersion + '/';

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
            if(patch.isFile()&&(mVerify.verify(patch))){
                patchList.add(patch);
            }else {
                Log.e("seele_bug_fix","verify fail or is dir"+patch.getAbsolutePath());
            }
        }
        if(patchList.size() != 0){
            File dexOptDir = new File(PATCH_DIR, DEX_OPT_DIR);
            dexOptDir.mkdir();
            mPatchLoader.load((File[]) patchList.toArray(),dexOptDir);
        }

    }


    static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            loadPatch();
        }
    };
    
    public static void loadPatch(File src){
        File dest = new File(PATCH_DIR, PATCH_FILE);
        if(src==null || !src.exists()){
            Log.e("seele_bug_fix","there is not exists file : "+src.getAbsolutePath());
            return;
        }
        if (dest.exists()&&!dest.delete()) {
            return;
        }

        if(!mVerify.verify(src)){
            Log.e("seele_bug_fix","verify fail"+src.getAbsolutePath());
            return;
        }

        try {
            FileTool.copyFile(src,dest);
            File dexOptDir = new File(PATCH_DIR, DEX_OPT_DIR);
            dexOptDir.mkdir();
            mPatchLoader.load(dest,dexOptDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void loadPatch(String path){
        if(path == null){
            Log.e("seele loadPatch:"," path is null");
            return;
        }

        if(path.startsWith("http")){
            new DownloadPatchThread(path).start();
            return;
        }

        File src = new File(path);
        loadPatch(src);
    }

    private static class DownloadPatchThread extends Thread
    {
        String mDownUrl;
        public DownloadPatchThread(String downUrl){
            mDownUrl = downUrl;
        }

        @Override
        public void run()
        {
            try
            {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    // 获得存储卡的路径
                    URL url = new URL(mDownUrl);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入浿
                    InputStream is = conn.getInputStream();

                    File file = new File(PATCH_DIR);
                    // 判断文件目录是否存在
                    if (!file.exists())
                    {
                        file.mkdir();
                    }
                    File apkFile = new File(PATCH_DIR, PATCH_FILE);
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    // 缓存
                    byte buf[] = new byte[1024];
                    Log.d("down patch from",mDownUrl);
                    do
                    {
                        int numread = is.read(buf);
                        if (numread <= 0)
                        {
                            Log.d("down patch "," complete");
                            mHandler.sendEmptyMessage(0);
                            break;
                        }
                        fos.write(buf, 0, numread);
                    } while (true);
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    };

    public static void cleanPatch(){
        if(mContext != null){
            File baseDir = new File(BASE_URL);
            FileTool.deleteDir(baseDir);
        }
    }


}
