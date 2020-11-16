import Dependencies.DevLib.moshi
import Dependencies.SupportLib.androidLib
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("kotlin-android")
}

android {
    compileSdkVersion(Versions.TARGET_ANDROID_SDK)

    defaultConfig {
        applicationId = "com.crypto.klinechart.app"
        minSdkVersion(Versions.MIN_ANDROID_SDK)
        targetSdkVersion(Versions.TARGET_ANDROID_SDK)
        versionCode = generateVersionCode()
        versionName = generateVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        maybeCreate("debug").apply {
            isMinifyEnabled = false
        }
        maybeCreate("release").apply {
            isMinifyEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        this.jvmTarget = "1.8"
    }
}


dependencies {
    lintChecks(files("../libs/lint.jar"))
    androidLib()
    implementation(project(":klinechart"))
    moshi()
}

fun generateVersionCode(): Int? {
    val prop = getPropertiesFromFileAndCmd()
    return prop["versionCode"].toString().toInt()
}

fun generateVersionName(): String? {
    val prop = getPropertiesFromFileAndCmd()
    return "${prop["major"]}.${prop["minor"]}"
}

fun getPropertiesFromFileAndCmd(): Properties = Properties()
    .apply { load(FileInputStream(File("app/version.properties"))) }
    .apply {
        //override the properties from command line
        setProperty("versionCode", properties.getOrDefault("versionCode", getProperty("versionCode")) as String)
    }