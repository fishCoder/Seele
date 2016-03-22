package com.fjwangjia.android.seele;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.security.auth.x500.X500Principal;

/**
 * Created by flb on 16/3/22.
 */
public class Verify {

    private static final X500Principal DEBUG_DN = new X500Principal(
            "CN=Android Debug,O=Android,C=US");


    private PublicKey mPublicKey;
    private boolean mDebuggable;

    public  Verify(Context context){
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            PackageInfo packageInfo = pm.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
            CertificateFactory certFactory = CertificateFactory
                    .getInstance("X.509");
            ByteArrayInputStream stream = new ByteArrayInputStream(
                    packageInfo.signatures[0].toByteArray());
            X509Certificate cert = (X509Certificate) certFactory
                    .generateCertificate(stream);
            mDebuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
            mPublicKey = cert.getPublicKey();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Seele Verify", "init", e);
        } catch (CertificateException e) {
            Log.e("Seele Verify", "init", e);
        }
    }


    boolean verify(File file){
        if (mDebuggable) {
            Log.d("verify()", "mDebuggable = true");
            return true;
        }

        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);

            JarEntry jarEntry = jarFile.getJarEntry("classes.dex");
            if (null == jarEntry) {
            }
            Certificate[] certs = jarEntry.getCertificates();
            if (certs == null) {
                return false;
            }
            return verify(certs);
        } catch (IOException e) {
            Log.e("verify", file.getAbsolutePath(), e);
            return false;
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (IOException e) {
                Log.e("verify", file.getAbsolutePath(), e);
            }
        }

    }


    private  boolean verify(Certificate[] certificates){
        if(certificates == null && certificates.length == 0){
            return false;
        }

        for (int i = certificates.length - 1; i >= 0; i--) {
            try {
                certificates[i].verify(mPublicKey);
                return true;
            } catch (Exception e) {

            }
        }

        return false;
    }
}
