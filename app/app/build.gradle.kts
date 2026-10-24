import java.io.File
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.Properties
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

abstract class AdbReverseTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    @get:Internal
    abstract val adbExecutable: RegularFileProperty

    @get:Input
    abstract val port: Property<Int>

    @get:Input
    abstract val enabledForLoopback: Property<Boolean>

    @TaskAction
    fun configureReverse() {
        if (!enabledForLoopback.get()) {
            logger.lifecycle("MoneyCare: API không dùng loopback; bỏ qua adb reverse.")
            return
        }
        val adb = adbExecutable.get().asFile.absolutePath
        val devicesOutput = ByteArrayOutputStream()
        val devicesResult = execOperations.exec {
            commandLine(adb, "devices")
            standardOutput = devicesOutput
            isIgnoreExitValue = true
        }
        if (devicesResult.exitValue != 0) {
            logger.warn("MoneyCare: không thể đọc danh sách ADB; bỏ qua adb reverse.")
            return
        }

        val onlineDevices = devicesOutput.toString()
            .lineSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith("List of devices") }
            .map { it.split(Regex("\\s+")) }
            .filter { columns -> columns.size >= 2 && columns[1] == "device" }
            .map { columns -> columns[0] }
            .toList()

        if (onlineDevices.isEmpty()) {
            logger.lifecycle("MoneyCare: chưa có thiết bị ADB online; bỏ qua adb reverse.")
            return
        }

        onlineDevices.forEach { serial ->
            execOperations.exec {
                commandLine(
                    adb, "-s", serial, "reverse",
                    "tcp:${port.get()}", "tcp:${port.get()}"
                )
            }
            logger.lifecycle("MoneyCare: adb reverse tcp:${port.get()} đã bật cho $serial")
        }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Load environment variables from .env file
val envProperties = Properties()
val envFile = File(rootDir, ".env")
if (envFile.exists()) {
    envFile.inputStream().use { input ->
        envProperties.load(input)
    }
}

// Prefer process environment variables in CI, then .env for local development.
fun environmentValue(name: String, defaultValue: String = ""): String =
    System.getenv(name)?.takeIf(String::isNotBlank)
        ?: envProperties.getProperty(name)?.takeIf(String::isNotBlank)
        ?: defaultValue

val debugApiBaseUrl = environmentValue("API_BASE_URL", "http://10.0.2.2:8081/api/")
val releaseApiBaseUrl = environmentValue("API_BASE_URL_RELEASE", "https://example.invalid/api/")
val apiTimeout = environmentValue("API_TIMEOUT", "30")
val apiLogLevel = environmentValue("API_LOG_LEVEL", "BODY")
val debugMode = environmentValue("DEBUG_MODE", "true")
val appVersion = environmentValue("APP_VERSION", "1.0")
val releaseStoreFile = environmentValue("RELEASE_STORE_FILE")
val releaseStorePassword = environmentValue("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = environmentValue("RELEASE_KEY_ALIAS")
val releaseKeyPassword = environmentValue("RELEASE_KEY_PASSWORD")
val releaseBuildRequested = gradle.startParameter.taskNames.any {
    it.contains("release", ignoreCase = true)
}
val releaseSigningConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all(String::isNotBlank)

fun isValidApiUrl(value: String, requireHttps: Boolean): Boolean = runCatching {
    val uri = URI(value)
    uri.host != null &&
        uri.path.endsWith('/') &&
        if (requireHttps) uri.scheme == "https" else uri.scheme in setOf("http", "https")
}.getOrDefault(false)

require(isValidApiUrl(debugApiBaseUrl, requireHttps = false)) {
    "API_BASE_URL must be an absolute HTTP(S) URL with a trailing /"
}
require(isValidApiUrl(releaseApiBaseUrl, requireHttps = true)) {
    "API_BASE_URL_RELEASE must use HTTPS and end with /"
}
require(apiTimeout.toLongOrNull()?.let { it > 0 } == true) {
    "API_TIMEOUT must be a positive whole number of seconds"
}
require(debugMode.equals("true", ignoreCase = true) || debugMode.equals("false", ignoreCase = true)) {
    "DEBUG_MODE must be true or false"
}
if (releaseBuildRequested) {
    require(releaseApiBaseUrl != "https://example.invalid/api/") {
        "Set API_BASE_URL_RELEASE to the real HTTPS production API before building release"
    }
    require(releaseSigningConfigured) {
        "Set RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS and RELEASE_KEY_PASSWORD before building release"
    }
    require(rootProject.file(releaseStoreFile).isFile) {
        "RELEASE_STORE_FILE does not point to an existing keystore"
    }
}

android {
    namespace = "com.moneycare.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.moneycare.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "API_TIMEOUT", "\"$apiTimeout\"")
        buildConfigField("String", "API_LOG_LEVEL", "\"$apiLogLevel\"")
        buildConfigField("Boolean", "DEBUG_MODE", debugMode)
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$debugApiBaseUrl\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$releaseApiBaseUrl\"")
            buildConfigField("Boolean", "DEBUG_MODE", "false")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            signingConfigs.findByName("release")?.let { signingConfig = it }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val debugApiUri = URI(debugApiBaseUrl)
val debugApiPort = debugApiUri.port.takeIf { it > 0 }
    ?: if (debugApiUri.scheme == "https") 443 else 80

val adbReverseDebug by tasks.registering(AdbReverseTask::class) {
    group = "moneycare"
    description = "Tự động reverse cổng API cho các thiết bị ADB trước khi chạy bản debug"
    adbExecutable.set(androidComponents.sdkComponents.adb)
    port.set(debugApiPort)
    enabledForLoopback.set(debugApiUri.host in setOf("127.0.0.1", "localhost"))
}

tasks.matching { it.name == "preDebugBuild" }.configureEach {
    dependsOn(adbReverseDebug)
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
}
