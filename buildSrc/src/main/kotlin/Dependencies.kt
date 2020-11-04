import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.exclude

object Dependencies {

    object SupportLib {
        fun DependencyHandler.androidLib() {
            implementation(ClassPaths.ANDROIDX_CORE_KTX)
            implementation(ClassPaths.ANDROIDX_APPCOMPAT)
            implementation(ClassPaths.ANDROID_MATERIAL)
            implementation(ClassPaths.ANDROIDX_CONSTRAINTLAYOUT)
            implementation(ClassPaths.ANDROIDX_NAVIGATION_FRAGMENT)
            implementation(ClassPaths.ANDROIDX_NAVIGATION_UI)
            implementation(ClassPaths.ANDROIDX_NAVIGATION_FRAGMENT_KTX)
            implementation(ClassPaths.ANDROIDX_NAVIGATION_UI_KTX)
            implementation(ClassPaths.ANDROIDX_LIFECYCLE_EXTENSION)
            implementation(ClassPaths.ANDROIDX_LEGACY_SUPPORT_V4)
            implementation(ClassPaths.COROUTINES_CORE)
            implementation(ClassPaths.COROUTINES_ANDROID)
            implementation(ClassPaths.COROUTINES_REACTIVE)
            implementation(ClassPaths.ANDROIDX_LIFECYCLE_LIVEDATA_KTX)
            implementation(ClassPaths.ANDROIDX_LIFECYCLE_RUNTIME_KTX)
            implementation(ClassPaths.ANDROIDX_LIFECYCLE_VIEWMODEL_KTX)
            implementation(ClassPaths.ANDROIDX_MULTIDEX)
            implementation(ClassPaths.ANDROIDX_ROOM_RUNTIME)
            kapt(ClassPaths.ANDROIDX_ROOM_COMPILER)
            implementation(ClassPaths.ANDROIDX_ROOM_KTX)
            implementation(ClassPaths.DATABASE_SQL_CIPHER)
            implementation(ClassPaths.ANDROIDX_VECTOR_DRAWABLE)
            implementation(ClassPaths.ANDROIDX_PAGING_RUNTIME)
            implementation(ClassPaths.ANDROIDX_PAGING_RUNTIME_KTX)
        }

    }

    // third-party lib
    object DevLib {
    }

    //testing
    object TestLib {

        fun DependencyHandler.robolectric() {
            testImplementation(ClassPaths.ROBOLECTRIC)
        }

        fun DependencyHandler.testingTools() {
            testImplementation(ClassPaths.JUNIT)
            androidTestImplementation(ClassPaths.ANDROIDX_TEST_EXTENSION_JUNIT)
            androidTestImplementation(ClassPaths.ANDROIDX_TEST_ESPRESSO_CORE)
            debugImplementation(ClassPaths.ANDROIDX_FRAGMENT_TESTING)
            androidTestImplementation(ClassPaths.ANDROIDX_TEST_CORE_KTX)
            androidTestImplementation(ClassPaths.BARISTA) {
                exclude(group = "org.jetbrains.kotlin")
            }
//            androidTestImplementation(ClassPaths.JUNIT5_JUPITER_API)

            testImplementation(ClassPaths.JUNIT5_JUPITER_API)
            testImplementation(ClassPaths.JUNIT5_JUPITER_ENGINE)
            testImplementation(ClassPaths.JUNIT5_VINTAGE_ENGINE)
            testImplementation(ClassPaths.MOCKK)
            androidTestImplementation(ClassPaths.ANDROID_MOCKK)
            testImplementation(ClassPaths.COROUTINES_TEST)
            testImplementation(ClassPaths.ANDROIDX_ROOM_TESTING)
            androidTestImplementation(ClassPaths.ANDROIDX_NAVIGATION_TESTING)
        }
    }
}

fun DependencyHandler.implementation(depNames: List<String>) {
    depNames.forEach { implementation(it) }
}

fun DependencyHandler.implementation(depName: String) {
    add("implementation", depName)
}

private fun DependencyHandler.kapt(depName: String) {
    add("kapt", depName)
}

private fun DependencyHandler.compileOnly(depName: String) {
    add("compileOnly", depName)
}

fun DependencyHandler.api(depNames: List<String>) {
    depNames.forEach { api(it) }
}

fun DependencyHandler.api(depName: String, closure: ExternalModuleDependency.() -> Unit = {}) {
    add("api", depName, closure)
}

fun DependencyHandler.testImplementation(depName: String) {
    add("testImplementation", depName)
}

fun DependencyHandler.androidTestImplementation(
    depName: String,
    closure: ExternalModuleDependency.() -> Unit = {}
) {
    add("androidTestImplementation", depName, closure)
}

fun DependencyHandler.debugImplementation(depName: String) {
    add("debugImplementation", depName)
}