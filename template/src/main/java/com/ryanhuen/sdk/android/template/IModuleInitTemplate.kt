package com.ryanhuen.sdk.android.template

import android.app.Application

interface IModuleInitTemplate {

    @MethodExit
    fun attachBaseContextMethodExit(application: Application)

    @MethodEnter
    fun onCreateMethodEnter()

    @MethodExit
    fun onCreateMethodExit()
}