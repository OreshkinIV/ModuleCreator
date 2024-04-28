// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.gradle.plugin-publish") version "1.2.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

version = "1.0.0"
group = "io.github.OreshkinIV"

gradlePlugin {
    website = "https://github.com/OreshkinIV/module-creator.git"
    vcsUrl = "https://github.com/OreshkinIV/module-creator.git"
    plugins {
        register("module-creator") {
            description = "Generates new module with clean architecture"
            displayName = "Module creator plugin"
            id = "io.github.OreshkinIV.module-creator"
            implementationClass = "io.github.OreshkinIV.ModuleCreatorPlugin"
            tags = listOf("creating modules")
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

signing {
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.2.2"
    distributionType = Wrapper.DistributionType.ALL
}
