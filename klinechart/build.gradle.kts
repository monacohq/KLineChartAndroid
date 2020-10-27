import Dependencies.SupportLib.androidLib
plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")

    //jitpack.io publish
    id("com.github.dcendents.android-maven")
}

//jitpack.io publish
group = "com.github.monacohq"
version = "1.0.0"

android {
    compileSdkVersion(Versions.TARGET_ANDROID_SDK)
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    androidLib()
}