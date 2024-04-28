package io.github.OreshkinIV

import io.github.OreshkinIV.extensions.ModuleCreatorPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import io.github.OreshkinIV.task.CreateModuleTask
import org.gradle.kotlin.dsl.register

class ModuleCreatorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val createModuleExtension = project.extensions.create<ModuleCreatorPluginExtension>("moduleCreator", project)

        project.tasks.register<CreateModuleTask>("module") {
            group = "Module generation"
            description = "Generates new module"

            modulePath.set(createModuleExtension.modulePath)
            basePackage.set(createModuleExtension.basePackage)

            createModuleExtension.commonGradle.orNull?.let { commonGradle.set(it) }
            apiServiceDiModulePackagePath.set(createModuleExtension.apiServiceDiModulePackagePath)

            syncShortcut.set(System.getenv("AS_SYNC_SHORTCUT") ?: "^(+O)")
            closeTerminalShortcut.set(System.getenv("AS_CLOSE_TERMINAL_SHORTCUT") ?: "+ {ESC}")
        }
    }
}
