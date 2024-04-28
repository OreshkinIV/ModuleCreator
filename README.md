Module Creator

plugin for creating modules with base structure via the terminal

usage example:

.\gradlew module --n=<nodule name> --type=<type> --lang=<lang>

.\gradlew module --n=cart // generates empty module "cart" with gradle.kts file
.\gradlew module --n=cart --type=BASE --lang=GROOVY // generates module "cart" with gradle.kt file and common clean architecture

parameters: 

--n parameter - module name

--type parameter EMPTY (default) or BASE

--lang parameter GROOVY or KOTLIN_DSL (default), language of settings and gradle files

EMPTY - creates empty module, BASE - creates module with common clean architecture and Retrofit + Dagger2

plugin extensions:

in your project build.gradle file

moduleCreator {
    modulePath.set(project.layout.projectDirectory.dir("modules")) // [dir for modules]
    basePackage.set("com.myCompany") // [base app package]

    commonGradle.set(project.layout.projectDirectory.file("common.gradle")) // [common gradle file the data from which will be applied to the module]
    apiServiceDiModulePackagePath.set("com.mycompany.commonnetwork.di.module.ApiServiceModule") // [path to common DI module which provides Retrofit object]
}

your api service module example:

package com.mycompany.commonnetwork.di.module.ApiServiceModule

@Module
class ApiServiceModule {
    @Singleton
    @Provides
    fun provideRetrofit(
        endpointProvider: EndpointRepository,
        gson: Gson,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit
            .Builder()
            .client(client)
            .baseUrl(endpointProvider.provideEndpoint().address)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(EnumConverterFactory())
            .build()
    }

}

then apiServiceDiModulePackagePath extension should be - apiServiceDiModulePackagePath.set("com.mycompany.commonnetwork.di.module.ApiServiceModule")


your common gradle file example :

plugins {
    alias(libs.plugins.android.lib)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.navigation.safeargs)
}

apply from: "${project.rootDir}/config/quality/quality.gradle"
apply from: "${project.rootDir}/config/android_commons.gradle"

dependencies {

    implementation(project(":commonnetwork"))

    // dagger 2
    implementation(libs.dagger)
    implementation(libs.dagger.android)
    implementation(libs.dagger.android.support)
    kapt(libs.dagger.android.processor)
    kapt(libs.dagger.compiler)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)

    // Network
    implementation(libs.retrofit)
}

sync project for Windows users:

plugin uses deafault Android Studio shortcut for sync project with gradle file

to change it for your shortcut use environment variables with PowerShell key codes

AS_SYNC_SHORTCUT - default AS value: ^(+O)
AS_CLOSE_TERMINAL_SHORTCUT - default AS value: + {ESC}  (it is necessary to change the focus from the terminal to the project)

list of key/code

https://learn.microsoft.com/en-us/previous-versions/office/developer/office-xp/aa202943(v=office.10)?redirectedfrom=MSDN