package io.github.OreshkinIV.task

import io.github.OreshkinIV.models.EmptyModuleDirs
import io.github.OreshkinIV.models.ModuleType
import io.github.OreshkinIV.models.GradleLanguage
import io.github.OreshkinIV.models.IncludeType
import io.github.OreshkinIV.utils.OsUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.property
import java.io.File

abstract class CreateModuleTask : DefaultTask() {

    private companion object {
        const val SOURCE_ROOT = "/src/main"
        const val SOURCE_KOTLIN = "kotlin"

        const val SOURCE_RESOURCES = "res"

        const val DRAWABLE = "drawable"
        const val LAYOUT = "layout"
        const val NAVIGATION = "navigation"
        const val VALUES = "values"

        const val DIMENS = "dimens.xml"
        const val STRINGS = "strings.xml"
        const val COLORS = "colors.xml"

        const val MANIFEST = "AndroidManifest.xml"

        const val DATA = "data"
        const val DOMAIN = "domain"
        const val PRESENTATION = "presentation"
        const val DI = "di"
        const val EXTENSIONS = "extensions"

        const val MODULE = "module"
        const val API_MODULE = "ApiModule"
        const val MODULE_PROVIDER = "ModuleProvider"

        const val MAPPERS = "mappers"
        const val REMOTE = "remote"
        const val RESPONSE = "response"
        const val PARAMS = "params"

        const val USECASE = "usecase"
        const val REPOSITORY = "repository"
        const val MODELS = "model"

        const val API_SERVICE_SUFFIX = "ApiService"
        const val REPOSITORY_SUFFIX = "Repository"
        const val REPOSITORY_IMPL_SUFFIX = "RepositoryImpl"

        const val KT_SUFFIX = ".kt"
        const val KTS_SUFFIX = ".kts"

        const val CONSUMER_RULES = "consumers-rules.pro"
        const val PROGUARD_RULES = "proguard-rules.pro"

        const val GIT_IGNORE = ".gitignore"
        const val DEFAULT_GIT_IGNORE_TEXT = "/build"

        const val SETTINGS_GRADLE = "settings.gradle"
        const val BUILD_GRADLE = "build.gradle"

        const val SCRIPTS_DIR = "/scripts"
        const val SYNC_SCRIPT_NAME = "sync.ps1"
    }

    @get: Input
    @get: Option(
        option = "type",
        description = "\ntype of module:\nEMPTY - empty module\nBASE - common clean architecture module with Dagger and Retrofit"
    )
    val moduleType: Property<ModuleType> = project.objects.property<ModuleType>().convention(ModuleType.EMPTY)

    @get: Input
    @get: Option(
        option = "lang", description = "\ngradle language: GROOVY or KOTLIN_DSL"
    )
    val language: Property<GradleLanguage> = project.objects.property<GradleLanguage>().convention(GradleLanguage.KOTLIN_DSL)

    @get: Input
    @get: Option(
        option = "include", description = "\nsettings include type: INCLUDE or INCLUDE_DIR"
    )
    val includeType: Property<IncludeType> = project.objects.property<IncludeType>().convention(IncludeType.INCLUDE_DIR)

    @get:OutputDirectory
    abstract val modulePath: DirectoryProperty

    @get:Option(option = "n", description = "module name")
    @get: Input
    abstract val moduleName: Property<String>

    @get: Input
    abstract val basePackage: Property<String>

    @get: Input
    abstract val apiServiceDiModulePackagePath: Property<String>

    @get:InputFile
    @get:Optional
    abstract val commonGradle: RegularFileProperty

    @get: Input
    abstract val syncShortcut: Property<String>

    @get: Input
    abstract val closeTerminalShortcut: Property<String>

    @TaskAction
    fun execute() {
        val moduleName = moduleName.get()
        val rootModuleDir = File(modulePath.get().asFile, moduleName)

        if (!rootModuleDir.exists()) {
            val emptyModuleDirs = createEmptyModule(rootModuleDir, moduleName)

            val modulePackage = emptyModuleDirs.modulePackage
            val sourceMainDir = emptyModuleDirs.sourceMainDir
            val modulePackagePath = emptyModuleDirs.modulePackagePath

            when (moduleType.get()) {
                ModuleType.BASE -> {
                    createCommonCleanArchitecture(moduleName, modulePackage, sourceMainDir, modulePackagePath)
                }

                else -> Unit // default empty module already created
            }

            val settingsFile: String = when (language.get()) {
                GradleLanguage.KOTLIN_DSL -> SETTINGS_GRADLE.plus(KTS_SUFFIX)
                GradleLanguage.GROOVY -> SETTINGS_GRADLE
            }

            val excludeRootDir = excludeRootDir()
            val includeDirText = "\n\nincludeDir(\":${moduleName}\", \"${excludeRootDir}/$moduleName\")"
            val includeText = "\n\ninclude(\":${moduleName}\")"

            val appendSettingsText = when (includeType.get()) {
                IncludeType.INCLUDE_DIR -> includeDirText
                IncludeType.INCLUDE -> includeText
            }

            project.layout.projectDirectory.file(settingsFile).asFile
                .appendText(appendSettingsText)

            println("Module $moduleName created")

            syncProject()

        } else {
            println("Module $moduleName already exists")
        }
    }

    private fun excludeRootDir(): String {
        var excludeRootDir = modulePath.get().toString().replace(project.layout.projectDirectory.toString(), "")
        if (excludeRootDir.startsWith("\\") || excludeRootDir.startsWith("/")) {
            excludeRootDir = excludeRootDir.removeRange(0, 1)
        }
        return excludeRootDir
    }

    private fun createEmptyModule(
        rootModuleDir: File,
        moduleName: String,
    ): EmptyModuleDirs {
        val sourceRootDir = File(rootModuleDir, SOURCE_ROOT).apply { mkdirs() }

        val basePackage = basePackage.get()
        val moduleNamePackage = moduleName.replace("-", ".").replace("_", ".")
        val modulePackage = "$basePackage.$moduleNamePackage"

        File(sourceRootDir, MANIFEST).writeText(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="$modulePackage">
               
                </manifest>""".trimIndent()
        )

        val sourceKotlinPath = "$sourceRootDir/$SOURCE_KOTLIN"
        val modulePackageDirs = modulePackage.replace(".", "/")
        val modulePackagePath = File(sourceKotlinPath, modulePackageDirs).apply { mkdirs() }

        File(rootModuleDir, GIT_IGNORE).writeText(DEFAULT_GIT_IGNORE_TEXT)

        File(rootModuleDir, CONSUMER_RULES).writeText("")
        File(rootModuleDir, PROGUARD_RULES).writeText("")

        val gradleFile: String = when (language.get()) {
            GradleLanguage.KOTLIN_DSL -> BUILD_GRADLE.plus(KTS_SUFFIX)
            GradleLanguage.GROOVY -> BUILD_GRADLE
        }
        val commonGradleText = commonGradle.orNull?.asFile?.readText().orEmpty()

        File(rootModuleDir, gradleFile).apply {
            writeText(commonGradleText)
            appendText(
                """
                
                
                android {
                    namespace = "$modulePackage"
                    resourcePrefix = "${moduleName}_"
                }""".trimIndent()
            )
        }

        return EmptyModuleDirs(sourceRootDir, modulePackagePath, modulePackage)
    }

    private fun cleanModuleName(moduleName: String): String {
        val moduleNameChars = moduleName.toCharArray()
        moduleName.forEachIndexed { index, char ->
            if ((char == '-' || char == '_') && index != moduleName.length - 1) {
                moduleNameChars[index + 1] = moduleNameChars[index + 1].uppercaseChar()
            }
        }
        return moduleNameChars.joinToString("").replace("_", "").replace("-", "")
    }

    private fun createCommonCleanArchitecture(
        moduleName: String,
        modulePackage: String,
        sourceMainDir: File,
        modulePackagePath: File
    ) {
        val cleanModuleName = cleanModuleName(moduleName)

        /** Domain ---------------------------------------------------------------------------------------------------------------------*/

        val domain = File(modulePackagePath, DOMAIN).apply { mkdir() }

        File(domain, MODELS).mkdir()
        File(domain, USECASE).mkdir()

        val domainRepository = File(domain, REPOSITORY).apply { mkdir() }

        val repository = cleanModuleName.plus(REPOSITORY_SUFFIX)
        val repositoryUppercase = repository.capitalized()
        val repositoryFileName = repositoryUppercase.plus(KT_SUFFIX)

        File(domainRepository, repositoryFileName).apply {
            writeText(
                """
            package $modulePackage.$DOMAIN.$REPOSITORY

            interface $repositoryUppercase {
            
            }
            
        """.trimIndent()
            )
        }

        /** Data -----------------------------------------------------------------------------------------------------------------------*/
        val data = File(modulePackagePath, DATA).apply { mkdir() }

        File(data, MAPPERS).mkdir()

        val remote = File(data, REMOTE).apply { mkdir() }

        File(remote, RESPONSE).mkdir()
        File(remote, PARAMS).mkdir()

        val apiService = cleanModuleName.plus(API_SERVICE_SUFFIX)
        val apiServiceUppercase = apiService.capitalized()
        val apiServiceFileName = apiServiceUppercase.plus(KT_SUFFIX)

        File(remote, apiServiceFileName).writeText(
            """
            package $modulePackage.$DATA.$REMOTE
            
            interface $apiServiceUppercase {
            
            }
            
        """.trimIndent()
        )

        val dataRepository = File(data, REPOSITORY).apply { mkdir() }

        val repositoryImpl = cleanModuleName.plus(REPOSITORY_IMPL_SUFFIX)
        val repositoryImplUppercase = repositoryImpl.capitalized()
        val repositoryImplFileName = repositoryImplUppercase.plus(KT_SUFFIX)

        File(dataRepository, repositoryImplFileName).writeText(
            """
            package $modulePackage.$DATA.$REPOSITORY
            
            import $modulePackage.$DATA.$REMOTE.$apiServiceUppercase
            import $modulePackage.$DOMAIN.$REPOSITORY.$repositoryUppercase
            import javax.inject.Inject
            
            class $repositoryImplUppercase @Inject constructor(
                private val $apiService: $apiServiceUppercase,
            ) : $repositoryUppercase {
            
            }
            
        """.trimIndent()
        )

        /** Presentation ---------------------------------------------------------------------------------------------------------------*/

        File(modulePackagePath, PRESENTATION).mkdir()

        /** DI -------------------------------------------------------------------------------------------------------------------------*/

        val di = File(modulePackagePath, DI).apply { mkdir() }
        val diModuleDir = File(di, MODULE).apply { mkdir() }

        val apiModule = cleanModuleName.plus(API_MODULE).capitalized()
        val apiModuleFileName = apiModule.plus(KT_SUFFIX)

        val apiServiceDiPackagePath = apiServiceDiModulePackagePath.get()
        val apiServiceDiClass = apiServiceDiPackagePath.split(".").last()

        File(diModuleDir, apiModuleFileName).writeText(
            """
            package $modulePackage.$DI.$MODULE
            
            import $modulePackage.$DATA.$REMOTE.$apiServiceUppercase
            import $apiServiceDiPackagePath
            import dagger.Module
            import dagger.Provides
            import retrofit2.Retrofit
            import javax.inject.Singleton
            
            @Module(includes = [$apiServiceDiClass::class])
            internal class $apiModule {

                @Provides
                @Singleton
                fun provide$apiServiceUppercase(
                    retrofit: Retrofit
                ): $apiServiceUppercase {
                    return retrofit.create($apiServiceUppercase::class.java)
                }
            }
            
        """.trimIndent()
        )

        val apiModuleProvider = cleanModuleName.plus(MODULE_PROVIDER).capitalized()
        val moduleProviderFileName = apiModuleProvider.plus(KT_SUFFIX)

        File(diModuleDir, moduleProviderFileName).writeText(
            """
            package $modulePackage.$DI.$MODULE
            
            import $modulePackage.$DATA.$REPOSITORY.$repositoryImplUppercase
            import $modulePackage.$DOMAIN.$REPOSITORY.$repositoryUppercase
            import dagger.Binds
            import dagger.Module
            
            @Module(
                includes = [
                    $apiModule::class,
                ]
            )
            abstract class $apiModuleProvider {
            
                @Binds
                abstract fun provide$repositoryUppercase($repository: $repositoryImplUppercase): $repositoryUppercase
            }
            
            """.trimIndent()
        )

        /** Extensions -----------------------------------------------------------------------------------------------------------------*/

        File(modulePackagePath, EXTENSIONS).mkdir()

        /** Resources ------------------------------------------------------------------------------------------------------------------*/

        val resFolder = File(sourceMainDir, SOURCE_RESOURCES).apply { mkdir() }

        File(resFolder, NAVIGATION).mkdir()
        File(resFolder, LAYOUT).mkdir()
        File(resFolder, DRAWABLE).mkdir()

        val emptyResContent = """
            <resources>
            </resources>
        """.trimIndent()

        val values = File(resFolder, VALUES).apply { mkdir() }
        File(values, STRINGS).writeText(emptyResContent)
        File(values, DIMENS).writeText(emptyResContent)
        File(values, COLORS).writeText(emptyResContent)
    }

    private fun syncProject() {
        val os = OsUtils.getOs()

        when (os) {
            OsUtils.OS.MAC -> {
                syncMac()
            }

            OsUtils.OS.WINDOWS -> {
                syncWindows()
            }

            else -> {}
        }
    }

    private fun syncMac() {
        project.exec {
            commandLine(
                "bash",
                "-c",
                "osascript -e \'delay 1\' -e \'tell application \"System Events\" to tell process \"Android Studio\" to click menu item \"Sync Project with Gradle Files\" of menu \"File\" of menu bar 1\'"
            )
        }
    }

    private fun syncWindows() {
        val scriptsDir = File(project.layout.projectDirectory.toString(), SCRIPTS_DIR)
        scriptsDir.mkdirs()

        val syncScript = File(scriptsDir, SYNC_SCRIPT_NAME)

        syncScript.apply {
            writeText(
                """
                param(
                  [string]${'$'}close,
                  [string]${'$'}sync
                )
                ${'$'}wsh = New-Object -ComObject WScript.Shell
                ${'$'}wsh.SendKeys(${'$'}close) 
                sleep 1
                ${'$'}wsh.SendKeys(${'$'}sync)
                """.trimIndent()
            )
            mkdirs()
        }

        project.exec {
            commandLine(
                "cmd",
                "/c",
                "Powershell -File ${syncScript.path} -sync \"${syncShortcut.get()}\" -close \"${closeTerminalShortcut.get()}\""
            )
        }
    }
}
