package com.ryanhuen.sdk.gradle.modinit.adapter

import com.ryanhuen.sdk.gradle.modinit.params.InjectClassParams
import com.ryanhuen.sdk.gradle.modinit.params.ModuleInitParams
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import java.io.File

class ModuleInitFieldSetupMethodAdapter(
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

    private fun injectTemplateMethodInvoke() {

        val list: List<InjectClassParams> = moduleInitParams.injectClassList
        for (injectClassParams in list) {
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitTypeInsn(NEW, injectClassParams.ctClass.name.replace(".", File.separator))
            mv.visitInsn(DUP)
            mv.visitMethodInsn(
                INVOKESPECIAL,
                injectClassParams.ctClass.name.replace(".", File.separator),
                "<init>",
                "()V",
                false
            )
            mv.visitFieldInsn(
                PUTFIELD,
                moduleInitParams.injectApplicationName.replace(".", File.separator),
                injectClassParams.fieldName,
                Type.getDescriptor(injectClassParams.clazz)
            )
            mv.visitVarInsn(ALOAD, 0)
            mv.visitEnd()
        }
    }

    override fun onMethodEnter() {
        super.onMethodEnter()
        injectTemplateMethodInvoke();
    }
}
