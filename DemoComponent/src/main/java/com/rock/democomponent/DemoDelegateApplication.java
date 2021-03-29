package com.rock.democomponent;

import android.app.Application;
import android.util.Log;

import com.ryanhuen.sdk.android.template.IModuleInitTemplate;

import org.jetbrains.annotations.NotNull;

public class DemoDelegateApplication implements IModuleInitTemplate {

    private static final DemoDelegateApplication sD = new DemoDelegateApplication();

    public static DemoDelegateApplication getInstance() {
        return sD;
    }

    @Override
    public void attachBaseContextMethodExit(@NotNull Application application) {
        Log.d("MuXi", "DemoDelegateApplication" + " : attachBaseContextMethodExit: " + application);
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
