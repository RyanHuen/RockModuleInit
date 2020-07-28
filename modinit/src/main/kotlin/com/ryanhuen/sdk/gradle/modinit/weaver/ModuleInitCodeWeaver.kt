package com.ryanhuen.sdk.gradle.modinit.weaver

import com.ryanhuen.sdk.gradle.modinit.adapter.ModuleInitClassAdapter
import com.ryanhuen.sdk.gradle.modinit.params.ModuleInitParams
import com.quinn.hunter.transform.asm.BaseWeaver
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class ModuleInitCodeWeaver : BaseWeaver() {

    private lateinit var moduleInitParams: ModuleInitParams

    fun attachData(moduleInitParams: ModuleInitParams) {
        this.moduleInitParams = moduleInitParams
    }

    override fun wrapClassWriter(classWriter: ClassWriter): ClassVisitor {
        return ModuleInitClassAdapter(
            classWriter,
            moduleInitParams
        )
    }
}