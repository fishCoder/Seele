package com.fjwangjia.android.seele;


import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collections;

import dalvik.system.DexClassLoader;

/**
 * Created by flb on 16/3/22.
 */
class PatchLoader {

    void load(File patch,File optDir){
        load(new File[]{patch},optDir);
    }
    void load(File[] patchs,File optDir){
        if(patchs == null || patchs.length == 0){
            Log.e("seele_bug_fix","file paths is empty");
            return;
        }
        String dexPath = "";
        for (File file : patchs){
            if(dexPath.isEmpty()){
                dexPath += file.getAbsolutePath();
            }else {
                dexPath += File.pathSeparator+file.getAbsolutePath();
            }

        }
        /*
        参考
        https://android.googlesource.com/platform/libcore/+/master/dalvik/src/main/java/dalvik/system
        DexPathList.java
        PathClassLoader.java
        BaseDexClassLoader.java
        etc
         */
        ClassLoader dexClassLoader = new DexClassLoader(dexPath, optDir.getAbsolutePath(),dexPath,this.getClass().getClassLoader());
        ClassLoader appClassLoader = getClass().getClassLoader();
        Class clazz;
        try {
            clazz = Class.forName("dalvik.system.BaseDexClassLoader");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Object appPathList = RefectTool.getFieldValue(clazz,appClassLoader,"pathList");
        Object dexPathList = RefectTool.getFieldValue(clazz,dexClassLoader,"pathList");

        if(appPathList == null || dexPathList==null){
            Log.e("Seele_PatchLoader","reflect dalvik.system.BaseDexClassLoader.pathList fail");
            return;
        }

        Object appEle = RefectTool.getFieldValue(appPathList.getClass(),appPathList,"dexElements");
        Object dexEle = RefectTool.getFieldValue(dexPathList.getClass(),dexPathList,"dexElements");

        if(appEle == null || dexEle==null){
            Log.e("Seele_PatchLoader","reflect PathList.dexElements fail");
            return;
        }

        int appEleLength = Array.getLength(appEle);
        int dexEleLength = Array.getLength(dexEle);

        Object dexElements = Array.newInstance(appEle.getClass().getComponentType(),appEleLength+dexEleLength);



        for (int i=0;i<appEleLength+dexEleLength;i++){
            if(i<dexEleLength){
                Array.set(dexElements,0,Array.get(dexEle,i));
            }else {
                Array.set(dexElements,dexEleLength,Array.get(appEle,i-dexEleLength));
            }
        }

        RefectTool.setFieldValue(dexPathList.getClass(),appPathList,"dexElements",dexElements);

        return;
    }
}
