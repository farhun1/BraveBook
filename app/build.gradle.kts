import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        allWarningsAsErrors = true
    }
}

extensions.configure<ApplicationExtension> {
    namespace = "com.hello.bravebook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hello.bravebook"
        minSdk = 23
        targetSdk = 36
        versionCode = 14
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sign with a real keystore when CI provides one via env vars
            // (KEYSTORE_FILE / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD).
            // Local dev builds fall back to the debug keystore — do NOT ship
            // a debug-signed artifact; CI must set the env vars above.
            signingConfig = signingConfigs.create("releaseFromEnv") {
                val ksPath = System.getenv("KEYSTORE_FILE")
                val ksPass = System.getenv("KEYSTORE_PASSWORD")
                val alias = System.getenv("KEY_ALIAS")
                val keyPass = System.getenv("KEY_PASSWORD")
                if (ksPath != null && ksPass != null && alias != null && keyPass != null) {
                    storeFile = file(ksPath)
                    storePassword = ksPass
                    this.keyAlias = alias
                    keyPassword = keyPass
                } else {
                    val d = signingConfigs.getByName("debug")
                    storeFile = d.storeFile
                    storePassword = d.storePassword
                    this.keyAlias = d.keyAlias
                    keyPassword = d.keyPassword
                }
            }
        }
        debug { applicationIdSuffix = ".test" }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(libs.compose.webview.multiplatform)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.playwright)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}