import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val tmdbReadToken = localProperties.getProperty("TMDB_READ_TOKEN", "").trim()
// Escape as a Java string literal without printing the credential.
val tmdbTokenLiteral = "\"" + tmdbReadToken
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r") + "\""

android {
    namespace = "com.kalidorabr.bancodeminutagens"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.kalidorabr.bancodeminutagens"
        minSdk = 26
        targetSdk = 37
        versionCode = 7
        versionName = "1.8"
        buildConfigField("String", "TMDB_READ_TOKEN", tmdbTokenLiteral)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Carimbo (assinatura) do app: lido de keystore.properties, que fica só neste computador
    val assinatura = rootProject.file("keystore.properties")
    val dadosAssinatura = Properties().apply { if (assinatura.exists()) assinatura.inputStream().use { load(it) } }
    signingConfigs {
        if (assinatura.exists()) create("release") {
            storeFile = rootProject.file(dadosAssinatura.getProperty("storeFile"))
            storePassword = dadosAssinatura.getProperty("storePassword")
            keyAlias = dadosAssinatura.getProperty("keyAlias")
            keyPassword = dadosAssinatura.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            proguardFiles("regras-otimizacao.pro")
            if (assinatura.exists()) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
