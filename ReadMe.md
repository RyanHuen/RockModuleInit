# RockModuleInit
## 方案：
- 组件化模块需要注入Application、获取Context等相关逻辑的解决方案

## 使用方式：
- 集成：

```
    implementation "com.ex.sdk.android:template:1.0.0"
    implementation "com.ex.sdk.android:modinit:1.0.0"
```

- app工程配置：
```
    apply plugin: 'mod_init'//使用mod_init插件，在代码编译过程中，会自动进行字节码插桩

    moduleInitExt {
        injectApplicationName "com.ryanhuen.exmoduleinit.TestApplication"  //app工程Manifest中配置的Application类全名
        scanPackageNames = ["com.rock", "com.ryanhuen"]   //插桩扫描的package包
    }
```

- 实现接口：

```java
public class DemoDelegateApplication implements IModuleInitTemplate {

    @Override
    public void attachBaseContext(@NotNull Application application) {

        Log.d("MuXi", "DemoDelegateApplication" + " : attachBaseContext: " + application);
    }

    @Override
    public void onCreateMethodEnter() {

        Log.d("MuXi", "DemoDelegateApplication" + " : onCreateMethodEnter: ");
    }

    @Override
    public void onCreateMethodExit() {

        Log.d("MuXi", "DemoDelegateApplication" + " : onCreateMethodExit: ");
    }
}
```


# 原理
- 简述原理：
  - 在编译过程中，mod_init插件，将会扫描工程中所有实现了IModuleInitTemplate.java文件的类
  - 将所有实现了上述接口的类，全部实例化到app工程的Application类中，并且在相应的位置，回调对应函数 
