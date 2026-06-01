plugins {
    alias(libs.plugins.android.application)
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
        minSdk = 28
        targetSdk = 36
        versionCode = 55
        versionName = "34.5.5"
        signingConfig = signingConfigs.getByName("main")  // not applied on debug
    }

    sourceSets.named("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        java.directories += "src/java"
        kotlin.directories += "src/kotlin"
        res.directories += "src/res"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    buildFeatures {
        buildConfig = true
        resValues = true
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

kotlin {
    compilerOptions {
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24
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
    }
    implementation(libs.hellocharts)
    implementation(libs.mcdtp)
}
