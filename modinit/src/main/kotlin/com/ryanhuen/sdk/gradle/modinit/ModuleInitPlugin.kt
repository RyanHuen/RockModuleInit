package com.ryanhuen.sdk.gradle.modinit

import com.android.build.gradle.AppExtension
import com.ryanhuen.sdk.gradle.modinit.extension.ModuleInitExt
import com.ryanhuen.sdk.gradle.modinit.transform.ModuleInitTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleInitPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        try {
            val dependenciesExtension = project.extensions.create(
                "moduleInitExt", ModuleInitExt::class.java
            )
            val appExtension = project.properties["android"] as AppExtension?
            appExtension?.registerTransform(
                ModuleInitTransform(
                    project,
                    appExtension,
                    dependenciesExtension
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}