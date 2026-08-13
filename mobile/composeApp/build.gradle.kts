import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.android.build.gradle.internal.dsl.SigningConfig
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.gradle.TargetConfigDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

val appVersionCode = 30100
val appVersionName = "3.1.0"

fun TargetConfigDsl.stringField(
    name: String,
    key: String = name,
) {
    val value = gradleLocalProperties(rootDir, providers).getProperty(key)
    buildConfigField(STRING, name, value ?: "")
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.aboutLibraries)
}

buildkonfig {
    packageName = "com.yagubogu"

    defaultConfigs {
        buildConfigField(BOOLEAN, "IS_DEBUG", "true")

        stringField("BASE_URL", key = "BASE_URL_DEBUG")
        stringField("WEB_CLIENT_ID")
        stringField("IOS_CLIENT_ID")
        stringField("DEBUG_FIXED_DATE")
        buildConfigField(INT, "VERSION_CODE", "$appVersionCode")

        // AdMob Ids
        buildConfigField(STRING, "ADMOB_ANDROID_APP_ID", "ca-app-pub-3940256099942544~3347511713")
        val testAndroidBannerId = "ca-app-pub-3940256099942544/9214589741"
        buildConfigField(STRING, "ADMOB_ANDROID_HOME_BANNER", testAndroidBannerId)
        buildConfigField(STRING, "ADMOB_ANDROID_RANKING_BANNER", testAndroidBannerId)
        buildConfigField(STRING, "ADMOB_ANDROID_LIVETALK_BANNER", testAndroidBannerId)
        buildConfigField(STRING, "ADMOB_ANDROID_STATS_BANNER", testAndroidBannerId)
        buildConfigField(STRING, "ADMOB_ANDROID_ATTENDANCE_CALENDAR_BANNER", testAndroidBannerId)
        buildConfigField(STRING, "ADMOB_ANDROID_EXIT_DIALOG_BANNER", testAndroidBannerId)
        buildConfigField(STRING, "ADMOB_ANDROID_PROFILE_DIALOG_BANNER", testAndroidBannerId)
        val testAndroidInterstitialId = "ca-app-pub-3940256099942544/1033173712"
        buildConfigField(
            STRING,
            "ADMOB_ANDROID_PAST_CHECK_IN_INTERSTITIAL",
            testAndroidInterstitialId,
        )
        val testIosBannerId = "ca-app-pub-3940256099942544/2934735716"
        buildConfigField(STRING, "ADMOB_IOS_HOME_BANNER", testIosBannerId)
        buildConfigField(STRING, "ADMOB_IOS_RANKING_BANNER", testIosBannerId)
        buildConfigField(STRING, "ADMOB_IOS_LIVETALK_BANNER", testIosBannerId)
        buildConfigField(STRING, "ADMOB_IOS_STATS_BANNER", testIosBannerId)
        buildConfigField(STRING, "ADMOB_IOS_ATTENDANCE_CALENDAR_BANNER", testIosBannerId)
        buildConfigField(STRING, "ADMOB_IOS_EXIT_DIALOG_BANNER", testIosBannerId)
        buildConfigField(STRING, "ADMOB_IOS_PROFILE_DIALOG_BANNER", testIosBannerId)
        val testIosInterstitialId = "ca-app-pub-3940256099942544/4411468910"
        buildConfigField(STRING, "ADMOB_IOS_PAST_CHECK_IN_INTERSTITIAL", testIosInterstitialId)
    }

    defaultConfigs("release") {
        buildConfigField(BOOLEAN, "IS_DEBUG", "false")

        stringField("BASE_URL", key = "BASE_URL_RELEASE")
        stringField("WEB_CLIENT_ID")
        stringField("IOS_CLIENT_ID")
        // AdMob Ids
        stringField("ADMOB_ANDROID_APP_ID", key = "ADMOB_ANDROID_APP_ID")
        stringField("ADMOB_ANDROID_HOME_BANNER")
        stringField("ADMOB_ANDROID_RANKING_BANNER")
        stringField("ADMOB_ANDROID_LIVETALK_BANNER")
        stringField("ADMOB_ANDROID_STATS_BANNER")
        stringField("ADMOB_ANDROID_ATTENDANCE_CALENDAR_BANNER")
        stringField("ADMOB_ANDROID_EXIT_DIALOG_BANNER")
        stringField("ADMOB_ANDROID_PROFILE_DIALOG_BANNER")
        stringField("ADMOB_ANDROID_PAST_CHECK_IN_INTERSTITIAL")
        stringField("ADMOB_IOS_HOME_BANNER")
        stringField("ADMOB_IOS_RANKING_BANNER")
        stringField("ADMOB_IOS_LIVETALK_BANNER")
        stringField("ADMOB_IOS_STATS_BANNER")
        stringField("ADMOB_IOS_ATTENDANCE_CALENDAR_BANNER")
        stringField("ADMOB_IOS_PROFILE_DIALOG_BANNER")
        stringField("ADMOB_IOS_PAST_CHECK_IN_INTERSTITIAL")
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // AndroidX Core
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.fragment.ktx)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.lifecycle.runtime.ktx)

            // Firebase
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics.ndk)

            // Google Credentials
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.google.googleid)

            // Google Services
            implementation(libs.play.services.location)

            // AdMob
            implementation(libs.ads.mobile.sdk)

            // Ktor
            implementation(libs.ktor.client.okhttp)

            // Play In-App Update
            implementation(libs.app.update)
            implementation(libs.app.update.ktx)

//            // Testing
//            testImplementation(libs.junit)
//            testImplementation(libs.kotest.runner.junit5)
//            testImplementation(libs.kotest.assertions.core)
//            testImplementation(libs.kotlinx.coroutines.test)
//            androidTestImplementation(libs.androidx.junit)
//            androidTestImplementation(libs.androidx.espresso.core)
//            androidTestImplementation(project.dependencies.platform(libs.androidx.compose.bom))
//            androidTestImplementation(libs.androidx.ui.test.junit4)

            // Debug
//            debugImplementation(libs.androidx.ui.tooling)
//            debugImplementation(libs.androidx.ui.test.manifest)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            // UI
            implementation(libs.material)
            implementation(libs.compressor)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonMain.dependencies {
            // AndroidX
            implementation(libs.androidx.datastore.preferences)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Kotlinx
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Ktor & Ktorfit
            implementation(libs.ktorfit.lib)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation3
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)

            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.imagepickerkmp)
            implementation(libs.zoomable)

            // UI Components
            implementation(libs.calendar.compose.multiplatform)

            // WebView
            implementation(libs.compose.webview)

            // Logging
            implementation(libs.kermit)
            implementation(libs.kermit.crashlytics)

            // Firebase Gitlive
            implementation(libs.firebase.config.gitlive)

            // Oss-licenses
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.m3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.yagubogu"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.yagubogu"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = appVersionCode
        versionName = appVersionName

        manifestPlaceholders["appName"] = "@string/app_name"
        manifestPlaceholders["admobAppId"] =
            gradleLocalProperties(rootDir, providers).getProperty("ADMOB_ANDROID_APP_ID") ?: ""
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
            manifestPlaceholders["appName"] = "야구보구.debug"
            // AdMob Test App ID
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
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
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", "de.jensklingenberg.ktorfit:ktorfit-ksp:2.7.2")
}

aboutLibraries {
    export {
        // "./gradlew :composeApp:exportLibraryDefinitions" 로 목록 생성
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
        prettyPrint = true
    }
}

ktorfit {
    compilerPluginVersion.set("2.3.3")
}

composeCompiler {
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("stability_config.conf"),
    )
}

// ktlint와 ksp 태스크 간의 순서를 명시적으로 지정하여 Gradle의 암시적 종속성 경고를 해결합니다.
// 생성된 코드는 .editorconfig의 ktlint 필터에서 제외되지만, 태스크 구조상 순서 정의가 필요합니다.
tasks.matching { it.name.contains("ktlint", ignoreCase = true) }.configureEach {
    mustRunAfter(tasks.matching { it.name.contains("ksp", ignoreCase = true) })
}

// 리소스를 복사하는 모든 태스크(Android, iOS) 전에 의존성 목록 생성 실행
tasks.matching { it.name.startsWith("copy") && it.name.contains("Resources") }.configureEach {
    dependsOn("exportLibraryDefinitions")
}

// Compose 리소스 클래스(Res) 생성 전에 의존성 목록 생성 실행 (IDE 인식용)
tasks.matching { it.name.contains("generateComposeResClass", ignoreCase = true) }.configureEach {
    dependsOn("exportLibraryDefinitions")
}
