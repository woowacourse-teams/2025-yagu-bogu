import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.build.gradle.internal.dsl.SigningConfig
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.oss.licenses.plugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.yagubogu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yagubogu"
        minSdk = 29
        targetSdk = 36
        versionCode = 2_01_00
        versionName = "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "BASE_URL_DEBUG",
            "\"${gradleLocalProperties(rootDir, providers).getProperty("BASE_URL_DEBUG")}\"",
        )
        buildConfigField(
            "String",
            "BASE_URL_RELEASE",
            "\"${gradleLocalProperties(rootDir, providers).getProperty("BASE_URL_RELEASE")}\"",
        )
        buildConfigField(
            type = "String",
            "WEB_CLIENT_ID",
            "\"${gradleLocalProperties(rootDir, providers).getProperty("WEB_CLIENT_ID")}\"",
        )

        val fixedDate = gradleLocalProperties(rootDir, providers).getProperty("DEBUG_FIXED_DATE")
        if (fixedDate != null) {
            buildConfigField("String", "DEBUG_FIXED_DATE", "\"$fixedDate\"")
        } else {
            buildConfigField("String", "DEBUG_FIXED_DATE", "null")
        }
    }

    val signingFile = rootProject.file("keystore.properties")
    val releaseSigningConfig: SigningConfig? =
        if (signingFile.exists()) {
            val keystoreProperties =
                Properties().apply {
                    load(FileInputStream(signingFile))
                }

            signingConfigs.create("release") {
                storeFile = file("yagubogu-keystore")
                keyAlias = "${keystoreProperties["KEY_ALIAS"]}"
                keyPassword = "${keystoreProperties["KEY_PASSWORD"]}"
                storePassword = "${keystoreProperties["KEYSTORE_PASSWORD"]}"
            }
        } else {
            null
        }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
            manifestPlaceholders["appName"] = "야구보구.debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            manifestPlaceholders["appName"] = "@string/app_name"
            if (releaseSigningConfig != null) {
                signingConfig = releaseSigningConfig
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.converter.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.viewpager2)
    implementation(libs.play.services.location)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.timber)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.glide)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.shimmer)
    implementation(libs.balloon)
    implementation(libs.balloon.compose)
    implementation(libs.ucrop)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics.ndk)

    // google credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.googleid)

    // play in-app update
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    // compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // navigation3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.navigation.compose)

    // coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // sse
    implementation(libs.okhttp)
    implementation(libs.okhttp.eventsource)

    // kotest
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
