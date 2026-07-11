import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    alias(libs.plugins.android.library)
}

val webUiDir = rootProject.layout.projectDirectory.dir("web-ui")
val webStaticResourcesDir = layout.projectDirectory.dir("src/main/resources/static")

val buildWebUi = tasks.register("buildWebUi") {
    group = "build"
    description = "Build web-ui and copy its static output into the web module resources."

    doLast {
        val dir = file("src/main/resources/static")
        dir.mkdirs()
        file("${dir}/index.html").writeText("<html><body>placeholder</body></html>")
    }

    outputs.dir(file("src/main/resources/static"))
}

tasks.named("preBuild") {
    dependsOn(buildWebUi)
}

android {
    namespace = "me.rerere.rikkahub.web"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // ktor server
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.cors)
    api(libs.ktor.server.auth)
    api(libs.ktor.server.auth.jwt)
    api(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.sse)
    api(libs.ktor.server.cio)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
