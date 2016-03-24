##Seele
在动画片 [EVA](http://baike.baidu.com/link?url=gqHTTxtNOOj_8fCc6YcJA_EtvBJy7rZa2A1o9GLbiuIHoM-ldhgN3NzK05GoCqCeYcxeA5RseK20O3iCR5EuKq)中, Seele 人类补完计划委员会首脑. 企图通过第三次冲击完成人类补完计划.


####Seele是一个基于mulitidex技术来实现给app打补丁的工具

###使用

##### 1.初始化  最好在application.onCreate()时调用

```
BugFix.init(context);
```

##### 2.加载一个File类型或者补丁的临时存储路径，当版本更新时会BugFix会自动清理上个版本的补丁，同一个版本中新load的补丁会覆盖之前的补丁
```
BugFix.loadPatch(file)
```
##### 3. 目前Seele还不能完全支持热补丁，有时候可能需要重启应用补丁才会生效


###生成补丁

[项目地址](https://github.com/fishCoder/ApkCompare)
[下载jar包](https://github.com/fishCoder/ApkCompare/releases/download/Release/ApkCompare-all-1.0.jar)

原理就是比较两个包然后抽取不同的类，打包并签名成补丁


```
java -jar ApkCompare -f <new apk> -t <old apk> -o <output dir> -k <keystore> -k <keystore password> -a <alias> -e <entry password>
```

###依赖包
```
compile 'com.fjwangjia.android:seele:1.0.1'
```
###栗子
```
git clone https://github.com/fishCoder/Seele.git
```
在asserts文件夹下有个补丁，运行程序后，因为打上了补丁，MainActivity将会显示text change 而不是 hello world

##支持版本

版本 | 测试 |
--------|------|
|  6.0	  | 通过|
|  5.0	  | 通过|
|  < 5.0 | 通过|