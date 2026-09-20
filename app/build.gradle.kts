/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { alias(libs.plugins.android.application) }

// Give priority to platform jars ahead of SDK
tasks.withType<KotlinCompile> {
    val platformJars =
        files("$rootDir/system_libs/framework.jar", "$rootDir/system_libs/framework-location.jar")
    doFirst {
        val list = buildList {
            add(platformJars)
            addAll(libraries.from)
        }
        libraries.setFrom(list)
    }
}

val productResDir = layout.buildDirectory.dir("generated/res/product-default")
val resolveProductRes by
    tasks.registering(Sync::class) {
        from("src/main/res")
        into(productResDir)
        filesMatching("**/strings.xml") {
            filter { line ->
                when {
                    line.contains("product=\"default\"") -> line.replace(" product=\"default\"", "")
                    line.contains("product=\"") -> ""
                    else -> line
                }
            }
        }
    }

tasks.named("preBuild") { dependsOn(resolveProductRes) }

android {
    namespace = "org.lineageos.setupwizard"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "org.lineageos.setupwizard"
        minSdk = 31
        targetSdk = 35
        versionCode = 20200
        versionName = "2.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets.named("main") {
        res.setSrcDirs(
            listOf(
                productResDir.get().asFile,
                "../../../../external/setupcompat/main/res",
                "../../../../external/setupcompat/partnerconfig/res",
                "../../../../external/setupdesign/main/res",
                "../../../../external/setupdesign/strings/res",
            )
        )
    }

    buildTypes { release { optimization { enable = false } } }
    buildTypes {
        getByName("release") {
            // Includes the default ProGuard rules files.
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
            )
        }
        getByName("debug") {
            // Append .dev to package name so we won't conflict with AOSP build.
            applicationIdSuffix = ".dev"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    compileOnly(fileTree(mapOf("dir" to "../system_libs", "include" to listOf("*.jar"))))

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.leanback)
    implementation(libs.lottie)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
