package com.ryanhuen.sdk.gradle.modinit.adapter

import com.ryanhuen.sdk.android.template.IModuleInitTemplate
import com.ryanhuen.sdk.gradle.modinit.params.InjectClassParams
import com.ryanhuen.sdk.gradle.modinit.params.ModuleInitParams
import org.objectweb.asm.*
import java.lang.reflect.Method

class ModuleInitClassAdapter(
    private val classWriter: ClassWriter,
    private val moduleInitParams: ModuleInitParams,
    private val templateMethods: Array<Method> = IModuleInitTemplate::class.java.methods
) : ClassVisitor(Opcodes.ASM7, classWriter) {

    private var mFieldVisited: Boolean = false

    private var mClassName: String = ""
    private var mSuperName: String? = null

    private var mAccess: Int = 0;

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        mAccess = access
        val className = name ?: ""
        if (moduleInitParams.hitInjectApplicationClass(className)) {
            mClassName = className
            mSuperName = superName

            if (!mClassName.isBlank()) {
                println("ModuleInitClassAdapter visit function call mFieldVisited: $mFieldVisited    mClassName :  $mClassName ")
                if (!mFieldVisited) {
                    mFieldVisited = true
                    attemptToInsertFieldFromInjectClassParams()
                }
            }
        } else {
            mClassName = ""
            mSuperName = ""
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {

        if (mClassName.isBlank()) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        } else {
            println("visit  inject对象内部函数： $name")
            val methodVisitor: MethodVisitor =
                cv.visitMethod(access, name, descriptor, signature, exceptions)
            if (name != null) {
                if (methodNameInTemplate(name)) {
                    return ModuleInitTemplateMethodAdapter(
                        mClassName,
                        moduleInitParams,
                        methodVisitor,
                        access,
                        name,
                        descriptor,
                        signature,
                        exceptions
                    )
                } else if (name == "<init>") {
                    println("找到类的init函数，执行插桩操作初始化插入的Field    类：$mClassName   函数：$name")
                    return ModuleInitFieldSetupMethodAdapter(
                        mClassName,
                        moduleInitParams,
                        methodVisitor,
                        access,
                        name,
                        descriptor,
                        signature,
                        exceptions
                    )
                }
            }
            return methodVisitor
        }
    }

    override fun visitEnd() {
        super.visitEnd()
    }

    private fun attemptToInsertFieldFromInjectClassParams() {
        println("ModuleInitClassAdapter attemptToInsertFieldFromInjectClassParams function call")
        val injectClassList: List<InjectClassParams> = moduleInitParams.injectClassList
        for (injectClassParams in injectClassList) {
            realInjectField(injectClassParams)
        }
    }

    private fun realInjectField(injectClassParams: InjectClassParams) {
        val fv = cv.visitField(
            Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL,
            injectClassParams.fieldName,
            Type.getDescriptor(injectClassParams.clazz),
            null,
            null
        );
        fv.visitEnd();

    }

    private fun methodNameInTemplate(name: String): Boolean {
        for (method in templateMethods) {
            if (method.name.startsWith(name)) {
                return true;
            }
        }
        return false
    }
}