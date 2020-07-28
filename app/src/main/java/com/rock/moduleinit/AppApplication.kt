package com.rock.moduleinit

import android.app.Application
import android.content.Context
import android.util.Log
import com.rock.democomponent.DemoDelegateApplication

class AppApplication(private var mDemoDelegateApplication: DemoDelegateApplication = DemoDelegateApplication()) :
    Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        mDemoDelegateApplication.onCreateMethodEnter()
        super.onCreate()
        Log.d("MuXi", "onCreate" + " : : ");
        mDemoDelegateApplication.onCreateMethodExit()
    }
}