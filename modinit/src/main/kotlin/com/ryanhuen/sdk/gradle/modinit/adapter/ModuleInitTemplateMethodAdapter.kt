package com.ryanhuen.sdk.gradle.modinit.adapter

import com.ryanhuen.sdk.android.template.IModuleInitTemplate
import com.ryanhuen.sdk.android.template.MethodEnter
import com.ryanhuen.sdk.android.template.MethodExit
import com.ryanhuen.sdk.gradle.modinit.params.InjectClassParams
import com.ryanhuen.sdk.gradle.modinit.params.ModuleInitParams
import javassist.CtClass
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import java.io.File

class ModuleInitTemplateMethodAdapter(
    val mClassName: String,
    val moduleInitParams: ModuleInitParams,
    methodVisitor: MethodVisitor,
    access: Int,
    name: String,
    descriptor: String?,
    signature: String?,
    exceptions: Array<out String>?
) :
    AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {

    private var enterMethodName: String = ""
    private var enterMethodWithParams: Boolean = false
    private var exitMethodName: String = ""
    private var exitMethodWithParams: Boolean = false

    init {
        setupEnterMethodName()
        setupExitMethodName()
    }

    private fun setupEnterMethodName() {

        val templateCtClass: CtClass =
            moduleInitParams.classPool.get(IModuleInitTemplate::class.java.name)
        for (method in templateCtClass.methods) {
            if (method.name.startsWith(name)
                && methodAnnotationHitWhatINeed(
                    method.annotations,
                    "@" + MethodEnter::class.java.name
                )
            ) {
                if (method.parameterTypes.isNotEmpty()) {
                    enterMethodWithParams = true
                }
                enterMethodName = method.name
                break
            }
        }
    }

    private fun setupExitMethodName() {
        val templateCtClass: CtClass =
            moduleInitParams.classPool.get(IModuleInitTemplate::class.java.name)
        for (method in templateCtClass.methods) {
            if (method.name.startsWith(name)
                && methodAnnotationHitWhatINeed(
                    method.annotations,
                    "@" + MethodExit::class.java.name
                )
            ) {
                if (method.parameterTypes.isNotEmpty()) {
                    enterMethodWithParams = true
                }
                exitMethodName = method.name
                break
            }
        }
    }

    private fun methodAnnotationHitWhatINeed(
        annotations: Array<Any>?,
        annotationsTarget: String
    ): Boolean {
        if (annotations != null) {
            for (methodAnnotation in annotations) {
                if (annotationsTarget == methodAnnotation.toString()) {
                    return true
                }
            }
        }
        return false
    }

    override fun onMethodEnter() {
        super.onMethodEnter()
        if (enterMethodName.isNotBlank()) {
            injectTemplateMethodInvokeEnter()
        }
    }

    private fun injectTemplateMethodInvokeEnter() {

        val list: List<InjectClassParams> = moduleInitParams.injectClassList
        for (injectClassParams in list) {
            val l0 = Label()
            mv.visitLabel(l0)
            mv.visitMethodInsn(
                INVOKESTATIC,
                injectClassParams.ctClass.name.replace(".", File.separator),
                "getInstance",
                "()" + Type.getDescriptor(injectClassParams.clazz),
                false
            )
            if (enterMethodWithParams) {
                mv.visitVarInsn(ALOAD, 0)
            }
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                injectClassParams.ctClass.name.replace(".", File.separator),
                enterMethodName,
                if (enterMethodWithParams) "(Landroid/app/Application;)V" else "()V",
                false
            )
        }
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        if (exitMethodName.isNotBlank()) {
            injectTemplateMethodInvokeExit()
        }
    }

    private fun injectTemplateMethodInvokeExit() {
        val list: List<InjectClassParams> = moduleInitParams.injectClassList
        for (injectClassParams in list) {
            val l0 = Label()
            mv.visitLabel(l0)
            mv.visitMethodInsn(
                INVOKESTATIC,
                injectClassParams.ctClass.name.replace(".", File.separator),
                "getInstance",
                "()" + Type.getDescriptor(injectClassParams.clazz),
                false
            )
            if (enterMethodWithParams) {
                mv.visitVarInsn(ALOAD, 0)
            }
            mv.visitMethodInsn(
                INVOKEVIRTUAL,
                injectClassParams.ctClass.name.replace(".", File.separator),
                exitMethodName,
                if (enterMethodWithParams) "(Landroid/app/Application;)V" else "()V",
                false
            )
        }
    }
}
