# Module Creator

Plugin for creating modules with base structure via the terminal. Creates a new module with a clean architecture containing the dagger 2 and retrofit template.

## Usage

### Applying the plugin

1. Add jitpack io repo in settings.gradle

```kotlin
pluginManagement {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

2. Edit libs.versions.toml file

```kotlin
module-creator = "1.0.0"

[plugins]
moduleCreator = { id = "com.github.OreshkinIV.module-creator", version.ref = "module-creator" }
```

3. Add plugin to plugins section

```kotlin
plugins {
  alias(libs.plugins.moduleCreator)
}
```

4. Сonfigure the plugin extensions:

```kotlin
moduleCreator {

    // folder for modules

    modulePath.set(project.layout.projectDirectory.dir("modules"))
 
    // if you don't have a folder for modules

    modulePath.set(project.layout.projectDirectory) 

    // base app package 

    basePackage.set("com.myCompany")

    // common gradle file the data from which will be applied to the gradle file of module

    commonGradle.set(project.layout.projectDirectory.file("common.gradle")) 

    // path to common DI module which provides Retrofit object

    apiServiceDiModulePackagePath.set("com.mycompany.commonnetwork.di.module.ApiServiceModule")
}
```

### your api service module example:

```kotlin
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
```

then apiServiceDiModulePackagePath extension should be 

```kotlin
apiServiceDiModulePackagePath.set("com.mycompany.commonnetwork.di.module.ApiServiceModule")
```

### your common gradle file example :

```kotlin
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
```

### Сalling a task:

* .\gradlew module --n=[module name] --type=[type] --lang=[lang] --include=[settings include type]

#### Call
* .\gradlew module --n=cart

#### Output 
empty module "cart" with gradle.kts file

#### Call
* .\gradlew module --n=cart --type=BASE --lang=GROOVY
  
#### Output 
generates module "cart" with gradle.kt file and common clean architecture (data/domain/presentation + dagger 2 and retrofit template)

#### task parameters: 

* --n parameter - module name

* --type parameter EMPTY (default) or BASE

EMPTY - creates empty module, BASE - creates module with common clean architecture and Retrofit + Dagger2

* --lang parameter - GROOVY or KOTLIN_DSL (default), language of settings and gradle files

* --include parameter - INCLUDE or INCLUDE_DIR (default)

INCLUDE_DIR: if you have a folder for modules (for example, "modules") - add to your setting file 

```kotlin
void includeDir(String moduleName, String modulePath) {
    include(moduleName)
    project(moduleName).projectDir = new File(rootDir, modulePath)
}
```

then after creating in settings.gradle will be added 

```kotlin
includeDir(":moduleName", "modules/moduleName")
```

or if you if you don't have a folder for modules, use

 .\gradlew module --n=module_name --include=INCLUDE 

for adding 
```kotlin 
include(:module_name)
``` 
to your settings.gradle after creating

### Sync project for Windows users:

plugin uses deafault Android Studio shortcut for sync project with gradle file

to change it for your shortcut use environment variables with PowerShell key codes

* AS_SYNC_SHORTCUT - default Android Studio value: ^(+O)

* AS_CLOSE_TERMINAL_SHORTCUT - default Android Studio value: + {ESC}  (it is necessary to change the focus from the terminal to the project)

[list of key/code](https://learn.microsoft.com/en-us/previous-versions/office/developer/office-xp/aa202943(v=office.10)?redirectedfrom=MSDN)