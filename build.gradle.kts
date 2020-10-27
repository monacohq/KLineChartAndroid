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
        classpath(ClassPaths.BUGSNAG_PLUGIN)
        classpath(ClassPaths.ANDROIDX_NAVIGATION_SAFE_ARGS_GRADLE_PLUGIN)
        classpath(ClassPaths.ANDROID_JUNIT_JACOCO_PLUGIN)
        classpath(ClassPaths.GOOGLE_SERVICE)
        classpath(ClassPaths.MAVEN_GRADLE_PLUGIN)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
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
    }
}

configurations.all {
    resolutionStrategy {
        force("org.antlr:antlr4-runtime:4.7.1")
        force("org.antlr:antlr4-tool:4.7.1")
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(rootProject.buildDir)
    }
}