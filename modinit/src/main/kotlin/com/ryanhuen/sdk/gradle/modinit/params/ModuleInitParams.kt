package com.ryanhuen.sdk.gradle.modinit.params

import javassist.ClassPool
import java.io.File

class ModuleInitParams(
    val injectApplicationName: String,
    val injectClassList: List<InjectClassParams>,
    val classPool: ClassPool
) {

    fun whetherClassInList(className: String): Boolean {
        val formatClassName = className.replace(
            File.separator, "."
        )
        var hit = false
        for (ctClass: InjectClassParams in injectClassList) {
            if (formatClassName == ctClass.ctClass.name) {
                hit = true
                break
            }
        }
        return hit
    }

    fun hitInjectApplicationClass(className: String): Boolean {
        val formatInjectApplicationName: String = injectApplicationName.replace(
            ".", "/"
        )
        return formatInjectApplicationName == className
    }

}