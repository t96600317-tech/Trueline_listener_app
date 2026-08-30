import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

val localProperties = Properties().also { properties ->
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use(properties::load)
    }
}

fun msg91Property(name: String): String = providers.gradleProperty(name)
    .orElse(providers.environmentVariable(name))
    .orElse(localProperties.getProperty(name) ?: "")
    .get()

val msg91WidgetId = msg91Property("MSG91_WIDGET_ID")
val msg91AuthToken = msg91Property("MSG91_AUTH_TOKEN")

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
    implementation("com.github.ZEGOCLOUD:zego_uikit_signaling_plugin_android:+")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.example.trueline_listener"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.nexvert.trueline_listner"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "MSG91_WIDGET_ID", buildConfigString(msg91WidgetId))
        buildConfigField("String", "MSG91_AUTH_TOKEN", buildConfigString(msg91AuthToken))
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
