import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "ir.mahdiparastesh.sexbook"
    compileSdk = 36
    buildToolsVersion = System.getenv("ANDROID_BUILD_TOOLS_VERSION")

    signingConfigs {
        create("main") {
            storeFile = file(System.getenv("JKS_PATH"))
            storePassword = System.getenv("JKS_PASS")
            keyAlias = "sexbook"
            keyPassword = System.getenv("JKS_PASS")
        }
    }

    defaultConfig {
        applicationId = "ir.mahdiparastesh.sexbook"
        minSdk = 26
        targetSdk = 36
        versionCode = 54
        versionName = "32.0.2"
        signingConfig = signingConfigs.getByName("main")  // not applied on debug
    }

    sourceSets.getByName("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        java.setSrcDirs(listOf("src/java"))
        kotlin.setSrcDirs(listOf("src/kotlin"))
        res.setSrcDirs(listOf("src/res"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }
    kotlin {
        target {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_24)
                freeCompilerArgs.add("-Xannotation-default-target=param-property")
            }
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Sexbook (debug)")
        }
        create("mahdi") {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.drawerlayout)
    implementation(libs.recyclerview)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.sqlite.ktx)
    implementation(libs.viewpager2)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.dotsindicator) {
        exclude(group = "androidx.activity", module = "activity-compose")
        exclude(group = "androidx.compose")
        exclude(group = "androidx.compose.ui")
        exclude(group = "androidx.compose.material3")
    }
    implementation(libs.hellocharts)
    implementation(libs.mcdtp)
}
