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
        versionCode = 51
        versionName = "28.1.9"
        signingConfig = signingConfigs.getByName("main") // not applied on debug

        testApplicationId = "ir.mahdiparastesh.sexbook.test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets.getByName("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        java.setSrcDirs(listOf("src/java"))
        kotlin.setSrcDirs(listOf("src/kotlin"))
        res.setSrcDirs(listOf("src/res"))
    }
    sourceSets.getByName("androidTest") {
        kotlin.setSrcDirs(listOf("srcTestAndroid"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_23
        targetCompatibility = JavaVersion.VERSION_23
    }
    kotlinOptions { jvmTarget = "23" }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        create("mahdi") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("main")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("main")
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.sqlite.ktx)
    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.hellocharts)
    implementation(libs.mcdtp)

    androidTestImplementation(libs.junit.android)
    androidTestImplementation(libs.espresso.core)
}
