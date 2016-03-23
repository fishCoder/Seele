package com.fjwangjia.android.hip;

import android.app.Application;
import android.content.Context;

import com.fjwangjia.android.seele.BugFix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by flb on 16/3/23.
 */
public class MyApp extends Application {



    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BugFix.init(base);
        try {
            BugFix.loadPatch(createFileFromInputStream(getBaseContext().getAssets().open("apk.patch")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createFileFromInputStream(InputStream inputStream) {

        try{
            File f = new File(getCacheDir()+"/apk.patch");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            //Logging exception
        }

        return null;
    }
}
