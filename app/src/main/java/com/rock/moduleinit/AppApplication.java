package com.rock.moduleinit;

import android.app.Application;
import android.content.Context;

import org.jetbrains.annotations.Nullable;

public class AppApplication extends Application {

    @Override
    protected void attachBaseContext(@Nullable Context base) {
        super.attachBaseContext(base);
//        DemoDelegateApplication.getInstance().attachBaseContextMethodExit((Application) this);
    }

    @Override
    public void onCreate() {
//        DemoDelegateApplication.getInstance().onCreateMethodEnter();
        super.onCreate();
//        DemoDelegateApplication.getInstance().onCreateMethodExit();
    }
}

