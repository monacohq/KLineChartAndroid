import Dependencies.SupportLib.androidLib
import java.io.FileInputStream
import java.util.Properties
plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("maven-publish")
}

android {
    compileSdkVersion(Versions.TARGET_ANDROID_SDK)
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    androidLib()
}

/**Create github.properties in root project folder file with gpr.usr=GITHUB_USER_ID  & gpr.key=PERSONAL_ACCESS_TOKEN**/
val githubProperties = Properties()
githubProperties.load(FileInputStream(rootProject.file("github.properties")))

publishing {
    publications {
        create<MavenPublication>("gpr") {
            run {
                groupId = "monacohq"
                artifactId = "klinechart"
                version = "1.0.4"
                artifact("$buildDir/outputs/aar/klinechart-release.aar")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/monacohq/KLineChartAndroid")
            credentials {
                username = githubProperties["gpr.usr"] as String
                password = githubProperties["gpr.key"] as String
            }
        }
    }
}