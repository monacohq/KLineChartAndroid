buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
    }
    dependencies {
        classpath(ClassPaths.GRADLE)
        classpath(ClassPaths.KOTLIN_GRADLE_PLUGIN)
        classpath(ClassPaths.ANDROIDX_NAVIGATION_SAFE_ARGS_GRADLE_PLUGIN)
        classpath(ClassPaths.ANDROID_JUNIT_JACOCO_PLUGIN)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts.kts files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven {
            setUrl("https://jitpack.io")
        }
    }
}
subprojects {
    apply(from = rootProject.file("ktlint.gradle.kts"))

    afterEvaluate {

        tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
            kotlinOptions {
                val options = this as org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
                options.jvmTarget = "1.8"
            }
        }

        extensions.run {
            configure(com.android.build.gradle.BaseExtension::class.java) {
                compileSdkVersion(Versions.TARGET_ANDROID_SDK)
                defaultConfig {
                    minSdkVersion(Versions.MIN_ANDROID_SDK)
                    targetSdkVersion(Versions.TARGET_ANDROID_SDK)

                    consumerProguardFiles("consumer-rules.pro")
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }

                buildTypes {
                    maybeCreate("debug").apply {
                        isMinifyEnabled = false
                        isDebuggable = true
                    }
                    maybeCreate("release").apply {
                        isMinifyEnabled = false
                        isDebuggable = false
                    }
                }
            }
        }
    }
}


tasks {
    val clean by registering(Delete::class) {
        delete(rootProject.buildDir)
    }
}