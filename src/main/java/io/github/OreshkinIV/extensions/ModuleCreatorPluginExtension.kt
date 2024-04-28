package io.github.OreshkinIV.extensions

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

abstract class ModuleCreatorPluginExtension(project: Project) {

    private val objects = project.objects

    val modulePath: DirectoryProperty = objects.directoryProperty()
        .convention(project.layout.projectDirectory.dir("modules"))

    val commonGradle: RegularFileProperty = objects.fileProperty()

    val basePackage: Property<String> = objects.property<String>().convention("")

    val apiServiceDiModulePackagePath: Property<String> = objects.property<String>()
}
