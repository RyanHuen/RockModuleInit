package com.ryanhuen.sdk.gradle.modinit.params

import javassist.CtClass

class InjectClassParams(var fieldName: String = "", var ctClass: CtClass) {
    var clazz: Class<*> = try {
        println("先尝试使用classLoader获取Class")
        ctClass.classPool.classLoader.loadClass(ctClass.name) as Class<*>
    } catch (e: ClassNotFoundException) {
        println("Class还没有加载，使用CtClass加载Class对象，此时会加载到ClassLoader中")
        ctClass.toClass()
    }
}