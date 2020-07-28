package com.ryanhuen.sdk.gradle.modinit.transform

import com.android.SdkConstants
import com.android.build.api.transform.Context
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.quinn.hunter.transform.HunterTransform
import com.ryanhuen.sdk.android.template.IModuleInitTemplate
import com.ryanhuen.sdk.gradle.modinit.extension.ModuleInitExt
import com.ryanhuen.sdk.gradle.modinit.params.InjectClassParams
import com.ryanhuen.sdk.gradle.modinit.params.ModuleInitParams
import com.ryanhuen.sdk.gradle.modinit.weaver.ModuleInitCodeWeaver
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.util.*
import java.util.jar.JarFile
import java.util.regex.Matcher

class ModuleInitTransform(
    val project: Project,
    val appExtension: AppExtension,
    val dependenciesExtension: ModuleInitExt
) : HunterTransform(project) {

    private val mAllClasses: MutableList<CtClass> = mutableListOf()
    private val mInjectClassSet: MutableSet<CtClass> = mutableSetOf()
    private var injectApplicationName: String = ""
    private var scanPackageNames: List<String> = listOf()
    private val classPool = ClassPool()

    init {
        this.bytecodeWeaver =
            ModuleInitCodeWeaver()
    }

    override fun transform(
        context: Context,
        inputs: MutableCollection<TransformInput>,
        referencedInputs: MutableCollection<TransformInput>,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        println("==========================$name start work===============================")
        println("attempt to setup injectApplicationName:  $injectApplicationName")
        this.injectApplicationName = dependenciesExtension.injectApplicationName
        this.scanPackageNames = dependenciesExtension.scanPackageNames
        onPreFindClasses(inputs)
        if (bytecodeWeaver is ModuleInitCodeWeaver) {
            println("try to attach data injectApplicationName:  $injectApplicationName")
            println("try to attach data mInjectClassList size :  ${mInjectClassSet.size}")
            (bytecodeWeaver as ModuleInitCodeWeaver).attachData(
                ModuleInitParams(
                    injectApplicationName,
                    transformInjectClassIntoParams(mInjectClassSet),
                    classPool
                )
            )
        }
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
    }

    private fun transformInjectClassIntoParams(mInjectClassSet: MutableSet<CtClass>): List<InjectClassParams> {
        val resultList: MutableList<InjectClassParams> = mutableListOf()
        val classList = mInjectClassSet.toList()
        for (ctClass in classList) {
            val filedName: String = "m" + ctClass.simpleName
            resultList.add(
                InjectClassParams(
                    filedName,
                    ctClass
                )
            )
        }
        return resultList.toList()
    }

    private fun onPreFindClasses(inputs: Collection<TransformInput>) {
        val classNames: MutableList<String> =
            ArrayList()
        val classpathList: List<File> = appExtension.bootClasspath
        for (file in classpathList) {
            try {
                classPool.appendClassPath(file.absolutePath)
            } catch (e: NotFoundException) {
                e.printStackTrace()
            }
        }
        for (input in inputs) {
            val directoryInputs =
                input.directoryInputs
            for (directoryInput in directoryInputs) {
                val parentPath = directoryInput.file.absolutePath
                val directoryInputFiles =
                    FileUtils.listFiles(directoryInput.file, null, true)
                try {
                    classPool.insertClassPath(parentPath)
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                }
                for (directoryInputFile in directoryInputFiles) {

                    if (directoryInputFile.absolutePath
                            .endsWith(SdkConstants.DOT_CLASS)
                    ) {
                        val fileAbsolutePath = directoryInputFile.absolutePath
                        val className = fileAbsolutePath.substring(
                            parentPath.length + 1,
                            fileAbsolutePath.length - SdkConstants.DOT_CLASS.length
                        ).replace(
                            Matcher.quoteReplacement(File.separator)
                                .toRegex(), "."
                        )
                        if (classNames.contains(className)) {
                            throw RuntimeException("You have duplicate classes with the same name : $className please remove duplicate classes ")
                        }
                        classNames.add(className)
                    }
                }
            }
            val jarInputs = input.jarInputs
            for (jarInput in jarInputs) {
                try {
                    classPool.insertClassPath(jarInput.file.absolutePath)
                    val jarFile = JarFile(jarInput.file)
                    val classes = jarFile.entries()
                    while (classes.hasMoreElements()) {
                        val libClass = classes.nextElement()
                        var className = libClass.name
                        if (className.endsWith(SdkConstants.DOT_CLASS)) {
                            className = className.substring(
                                0,
                                className.length - SdkConstants.DOT_CLASS.length
                            ).replace("/".toRegex(), ".")
                            if (classNames.contains(className)) {
                                throw RuntimeException("You have duplicate classes with the same name : $className please remove duplicate classes ")
                            }
                            classNames.add(className)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            for (className in classNames) {
                try {
                    mAllClasses.add(classPool[className])
                } catch (e: NotFoundException) {
                    println("pre load classes not found$className")
                }
            }
        }
        println("pre load classes finish, found  classes count : " + mAllClasses.size)
        mAllClasses.forEach { clazz ->
            if (hitTargetScanPackageName(clazz.packageName) && !clazz.isInterface && !clazz.interfaces.isNullOrEmpty()) {
                clazz.interfaces.forEach {
                    if (IModuleInitTemplate::class.qualifiedName.equals(it.name)) {
                        mInjectClassSet.add(clazz);
                    }
                }
            }
        }

        println("found template implement classes count :  " + mInjectClassSet.size);
        for (ctClass in mInjectClassSet) {
            println("found template implement classes :  $ctClass");
        }
    }

    private fun hitTargetScanPackageName(packageName: String?): Boolean {
        var hit: Boolean = false
        for (scanPackageName in scanPackageNames) {
            hit = packageName?.startsWith(scanPackageName) ?: false
            if (hit) {
                break
            }
        }
        return hit
    }

    override fun getName(): String {
        return "ex_module_init_transformer"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun getScopes(): MutableSet<QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }
}
