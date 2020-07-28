package com.ryanhuen.sdk.gradle.modinit.extension

open class ModuleInitExt {
    var injectApplicationName: String = ""

    var scanPackageNames: List<String> = listOf()

    fun injectApplicationName(injectApplicationName: String) {
        this.injectApplicationName = injectApplicationName
    }

    fun scanPackageNames(scanPackageNames: List<String>) {
        this.scanPackageNames = scanPackageNames
    }
}