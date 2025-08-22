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
}

android {
    namespace = "com.yagubogu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yagubogu"
        minSdk = 29
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.0"

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
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.timber)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.glide)
    implementation(libs.play.services.oss.licenses)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics.ndk)

    // google credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
