import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "ir.mahdiparastesh.sexbook"
    compileSdk = 37
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
        minSdk = 29
        targetSdk = 37
        versionCode = 55
        versionName = "34.6.4"
        signingConfig = signingConfigs.getByName("main")  // not applied on debug
    }

    sourceSets.named("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        java.directories += "src/java"
        kotlin.directories += "src/kotlin"
        res.directories += "src/res"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    kotlin { target { compilerOptions { jvmTarget.set(JvmTarget.JVM_25) } } }

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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // debuggability will cause obfuscation to occur partially.
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
    implementation(libs.dropbox.android)
    implementation(libs.dropbox.core)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.dotsindicator) {
        exclude("androidx.activity", "activity-compose")
        exclude("androidx.compose")
        exclude("androidx.compose.ui")
        exclude("androidx.compose.material3")
        exclude("androidx.core")
    }
    implementation(libs.hellocharts)
    implementation(libs.mcdtp)
}
