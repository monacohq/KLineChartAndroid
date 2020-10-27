object ClassPaths {

    // android core
    const val GRADLE = "com.android.tools.build:gradle:${Versions.GRADLE}"
    const val KOTLIN_GRADLE_PLUGIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}"
    const val ANDROIDX_CORE_KTX = "androidx.core:core-ktx:${Versions.ANDROIDX_CORE_KTX}"
    const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat:${Versions.ANDROIDX_APPCOMPAT}"
    const val ANDROID_MATERIAL = "com.google.android.material:material:${Versions.ANDROID_MATERIAL}"

    const val ANDROIDX_CONSTRAINTLAYOUT =
        "androidx.constraintlayout:constraintlayout:${Versions.ANDROIDX_CONSTRAINTLAYOUT}"
    const val ANDROIDX_NAVIGATION_FRAGMENT =
        "androidx.navigation:navigation-fragment:${Versions.ANDROIDX_NAVIGATION}"
    const val ANDROIDX_NAVIGATION_UI =
        "androidx.navigation:navigation-ui:${Versions.ANDROIDX_NAVIGATION}"
    const val ANDROIDX_NAVIGATION_FRAGMENT_KTX =
        "androidx.navigation:navigation-fragment-ktx:${Versions.ANDROIDX_NAVIGATION}"
    const val ANDROIDX_NAVIGATION_UI_KTX =
        "androidx.navigation:navigation-ui-ktx:${Versions.ANDROIDX_NAVIGATION}"
    const val ANDROIDX_LIFECYCLE_EXTENSION =
        "androidx.lifecycle:lifecycle-extensions:${Versions.ANDROIDX_LIFECYCLE}"
    const val ANDROIDX_LEGACY_SUPPORT_V4 =
        "androidx.legacy:legacy-support-v4:${Versions.ANDROIDX_LEGACY_SUPPORT}"
    const val ANDROIDX_LIFECYCLE_LIVEDATA_KTX =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.ANDROIDX_LIFECYCLE}"
    const val ANDROIDX_LIFECYCLE_RUNTIME_KTX =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.ANDROIDX_LIFECYCLE}"
    const val ANDROIDX_LIFECYCLE_VIEWMODEL_KTX =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.ANDROIDX_LIFECYCLE}"
    const val ANDROIDX_TEST_EXTENSION_JUNIT =
        "androidx.test.ext:junit:${Versions.ANDROIDX_TEST_EXTENSION_JUNIT}"
    const val ANDROIDX_TEST_ESPRESSO_CORE =
        "androidx.test.espresso:espresso-core:${Versions.ANDROIDX_TEST_ESPRESSO_CORE}"
    const val ANDROIDX_TEST_CORE_KTX = "androidx.test:core-ktx:${Versions.ANDROIDX_CORE_KTX}"
    const val ANDROIDX_MULTIDEX = "androidx.multidex:multidex:${Versions.ANDROIDX_MULTIDEX}"
    const val ANDROIDX_ROOM_RUNTIME = "androidx.room:room-runtime:${Versions.ANDROIDX_ROOM}"
    const val ANDROIDX_ROOM_COMPILER = "androidx.room:room-compiler:${Versions.ANDROIDX_ROOM}"
    const val ANDROIDX_ROOM_KTX = "androidx.room:room-ktx:${Versions.ANDROIDX_ROOM}"
    const val ANDROIDX_ROOM_TESTING = "androidx.room:room-testing:${Versions.ANDROIDX_ROOM}"
    const val DATABASE_SQL_CIPHER = "net.zetetic:android-database-sqlcipher:${Versions.DATABASE_SQL_CIPHER}"
    const val ANDROIDX_VECTOR_DRAWABLE =
        "androidx.vectordrawable:vectordrawable:${Versions.ANDROIDX_VECTOR_DRAWABLE}"
    const val ANDROIDX_NAVIGATION_SAFE_ARGS_GRADLE_PLUGIN = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.ANDROIDX_NAVIGATION}"
    const val ANDROIDX_PAGING_RUNTIME = "androidx.paging:paging-runtime:${Versions.ANDROIDX_PAGING}"
    const val ANDROIDX_PAGING_RUNTIME_KTX = "androidx.paging:paging-runtime-ktx:${Versions.ANDROIDX_PAGING}"
    const val ANDROIDX_NAVIGATION_TESTING = "androidx.navigation:navigation-testing:${Versions.ANDROIDX_NAVIGATION}"

    // coroutine
    const val COROUTINES_TEST =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.COROUTINES}"
    const val COROUTINES_CORE =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}"
    const val COROUTINES_ANDROID =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}"
    const val COROUTINES_REACTIVE =
        "org.jetbrains.kotlinx:kotlinx-coroutines-reactive:${Versions.COROUTINES}"

    // rxjava
    const val RXJAVA2_RXANDROID: String =
        "io.reactivex.rxjava2:rxandroid:${Versions.RXJAVA2_RXANDROID}"
    const val RXJAVA2_RXJAVA = "io.reactivex.rxjava2:rxjava:${Versions.RXJAVA2_RXJAVA}"

    // ui test and unit
    const val BARISTA = "com.schibsted.spain:barista:${Versions.BARISTA}"
    const val ANDROIDX_FRAGMENT_TESTING =
        "androidx.fragment:fragment-testing:${Versions.FRAGMENT_TESTING}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val JUNIT5_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT5}"
    const val JUNIT5_JUPITER_API = "org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT5}"
    const val JUNIT5_VINTAGE_ENGINE =
        "org.junit.vintage:junit-vintage-engine:${Versions.JUNIT5_VINTAGE}"
    const val MOCKK = "io.mockk:mockk:${Versions.MOCKK}"
    const val ANDROID_MOCKK = "io.mockk:mockk-android:${Versions.MOCKK}"

    // scarlet
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
    const val KOTLIN_REFLECT = "org.jetbrains.kotlin:kotlin-reflect:${Versions.KOTLIN}"
    const val SCARLET = "com.tinder.scarlet:scarlet:${Versions.SCARLET}"
    const val SCARLET_STREAM_ADAPTER =
        "com.tinder.scarlet:stream-adapter-rxjava2:${Versions.SCARLET}"
    const val SCARLET_MOSHI = "com.tinder.scarlet:message-adapter-moshi:${Versions.SCARLET}"
    const val SCARLET_LIFECYCLE = "com.tinder.scarlet:lifecycle-android:${Versions.SCARLET}"
    const val SCARLET_WEBSERVER = "com.tinder.scarlet:websocket-mockwebserver:${Versions.SCARLET}"
    const val SCARLET_COROUTINE = "com.tinder.scarlet:stream-adapter-coroutines:${Versions.SCARLET}"

    //moshi
    const val MOSHI_KOTLIN = "com.squareup.moshi:moshi-kotlin:${Versions.MOSHI}"
    const val MOSHI_ADAPTERS = "com.squareup.moshi:moshi-adapters:${Versions.MOSHI}"

    const val CALLIGRAPHY = "io.github.inflationx:calligraphy3:${Versions.CALLIGRAPHY}"
    const val VIEWPUMP = "io.github.inflationx:viewpump:${Versions.VIEWPUMP}"

    // philology
    const val PHILOLOGY = "com.jcminarro:Philology:${Versions.PHILOLOGY}"

    //bugsnag
    const val BUGSNAG_PLUGIN = "com.bugsnag:bugsnag-android-gradle-plugin:${Versions.BUGSNAG_PLUGIN}"
    const val BUGSNAG_ANDROID = "com.bugsnag:bugsnag-android:${Versions.BUGSNAG_ANDROID}"

    // koin
    const val KOIN_ANDROID = "org.koin:koin-android:${Versions.KOIN}"
    const val KOIN_ANDROIDX_SCOPE = "org.koin:koin-androidx-scope:${Versions.KOIN}"
    const val KOIN_ANDROIDX_VIEWMODEL = "org.koin:koin-androidx-viewmodel:${Versions.KOIN}"
    const val KOIN_ANDROIDX_FRAGMENT = "org.koin:koin-androidx-fragment:${Versions.KOIN}"
    const val KOIN_TEST = "org.koin:koin-test:${Versions.KOIN}"

    // Timber
    const val TIMBER = "com.jakewharton.timber:timber:${Versions.TIMBER}"

    // Retrofit
    const val RETROFIT2_RETROFIT = "com.squareup.retrofit2:retrofit:${Versions.RETROFIT}"
    const val RETROFIT2_CONVERTER_MOSHI = "com.squareup.retrofit2:converter-moshi:${Versions.RETROFIT}"
    const val RETORFIT2_MOCKWEBSERVER = "com.squareup.okhttp3:mockwebserver:${Versions.MOCKWEBSERVER}"

    // Chart
    const val MP_ANDROID_CHART = "com.github.PhilJay:MPAndroidChart:v${Versions.MP_ANDROID_CHART}"

    // Jacoco
    const val ANDROID_JUNIT_JACOCO_PLUGIN = "com.vanniktech:gradle-android-junit-jacoco-plugin:${Versions.ANDROID_JUNIT_JACOCO_PLUGIN}"

    // Google Service
    const val GOOGLE_SERVICE = "com.google.gms:google-services:${Versions.GOOGLE_SERVICE}"

    // Maven
    const val MAVEN_GRADLE_PLUGIN = "com.github.dcendents:android-maven-gradle-plugin:${Versions.MAVEN_GRADLE_PLUGIN}"
}