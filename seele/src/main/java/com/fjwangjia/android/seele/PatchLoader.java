package com.fjwangjia.android.seele;


import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by flb on 16/3/22.
 */
class PatchLoader {

    void load(File patch){
        load(new File[]{patch});
    }
    void load(File[] patchs){
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
        ClassLoader dexClassLoader = new DexClassLoader(dexPath,null,null,this.getClass().getClassLoader());
        ClassLoader appClassLoader = getClass().getClassLoader();
        Class clazz = dalvik.system.BaseDexClassLoader.class;
        Object appPathList = RefectTool.getFieldValue(clazz,appClassLoader,"pathList");
        Object dexPathList = RefectTool.getFieldValue(clazz,dexClassLoader,"pathList");

        Object appEle = RefectTool.getFieldValue(appClassLoader.getClass(),appPathList,"dexElements");
        Object dexEle = RefectTool.getFieldValue(appClassLoader.getClass(),dexPathList,"dexElements");

        int appEleLength = Array.getLength(appEle);
        int dexEleLength = Array.getLength(dexEle);

        Object dexElements = Array.newInstance(appEle.getClass().getComponentType(),appEleLength+dexEleLength);
        Array.set(dexElements,0,dexEleLength-1);
        Array.set(dexElements,dexEleLength,dexEleLength+appEleLength-1);

        RefectTool.setFieldValue(dexPathList.getClass(),dexPathList,"dexElements",dexElements);
    }
}
