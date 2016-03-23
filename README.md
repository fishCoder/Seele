[中文]()

##Seele
in [Evangelion](https://en.wikipedia.org/wiki/Evangelion_(mecha)), Seele is the Human Instrumentality Committee Seele. intends to complete the Human Instrumentality Project by intentionally initiating the Third Impact.


####Seele that a tool puts a patch is base on the MulitiDex

###How to use

##### 1.init  Had better to init on application.onCreate()

```
BugFix.init(context);
```

##### 2.load  load a file or url, when you down a patch from server,you could call it. when apk update version , BugFix will clean the patch of last version.
```
BugFix.loadPatch(file)
```
##### 3. So far,Seele is not support the hot fix. after calling BugFix.loadPatch(),Sometime put a patch successfully unitl App restarted


###Generate a patch

[Project_Address](https://github.com/fishCoder/ApkCompare)
[Download_Jar](https://github.com/fishCoder/ApkCompare/releases/download/Release/ApkCompare-all-1.0.jar)

Compare the two apks , extract the different parts


```
java -jar ApkCompare -f \<new apk> -t \<old apk> -o \<output dir> -k \<keystore> -k \<keystore password> -a \<alias> -e \<entry password>
```

###dependencies
```
compile 'com.fjwangjia.android:seele:1.0.1'
```


##Android Support

version | test |
--------|------|
|  6.0	  | pass|
|  5.0	  | pass|
|  < 5.0 | pass|