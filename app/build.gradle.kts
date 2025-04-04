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
        versionCode = 52
        versionName = "28.4.3"
        signingConfig = signingConfigs.getByName("main")  // not applied on debug
    }

    sourceSets.getByName("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        java.setSrcDirs(listOf("src/java"))
        kotlin.setSrcDirs(listOf("src/kotlin"))
        res.setSrcDirs(listOf("src/res"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    kotlinOptions { jvmTarget = "23" }

    buildFeatures {
        buildConfig = false
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
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.hellocharts)
    implementation(libs.mcdtp)
}
