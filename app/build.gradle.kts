plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ir.mahdiparastesh.sexbook"
    compileSdk = 35
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
        targetSdk = 35
        versionCode = 47
        versionName = "26.8.8"
        signingConfig = signingConfigs.getByName("main")
    }
    sourceSets.getByName("main") {
        manifest.srcFile("src/AndroidManifest.xml")
        java.setSrcDirs(listOf("src/java"))
        kotlin.setSrcDirs(listOf("src/kotlin"))
        res.setSrcDirs(listOf("src/res"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_22; targetCompatibility = JavaVersion.VERSION_22
    }
    kotlinOptions { jvmTarget = "22" }

    buildFeatures { buildConfig = true; viewBinding = true }
    buildTypes {
        create("debuggee") {
            isDebuggable = true
            isMinifyEnabled = false
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
androidComponents.beforeVariants { variantBuilder ->
    if (variantBuilder.buildType in listOf("debug", "androidTest"))
        variantBuilder.enable = false
}

dependencies {
    val roomVersion = "2.6.1"

    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("ir.mahdiparastesh:hellocharts:2.8.5")
    implementation("ir.mahdiparastesh:mcdtp:3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
