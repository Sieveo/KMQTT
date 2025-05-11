import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("convention.publication")
    id("org.jetbrains.kotlinx.atomicfu")
}

kotlin {
    explicitApi()
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    js {
        browser()
        nodejs {
            binaries.executable()
        }
    }
    mingwX64 {}
    linuxX64 {}
    linuxArm64 {}
    iosX64 {}
    iosArm64 {}
    iosSimulatorArm64 {}
    macosX64 {}
    macosArm64 {}
    tvosX64 {}
    tvosSimulatorArm64 {}
    tvosArm64 {}
    watchosArm32 {}
    watchosArm64 {}
    watchosSimulatorArm64 {}
    watchosX64 {}

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                api(project(":kmqtt-common"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.cio)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmAndNativeMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.network)
                implementation(libs.ktor.network.tls)
            }
        }

        val jvmMain by getting {
            dependsOn(jvmAndNativeMain)
            dependencies {
                implementation("ch.qos.logback:logback-classic:1.5.18")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val mingwX64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val linuxX64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val linuxArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val iosX64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val iosArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val macosX64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val macosArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val tvosX64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val tvosArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val tvosSimulatorArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val watchosX64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val watchosArm32Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val watchosArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }
        val watchosSimulatorArm64Main by getting {
            dependsOn(jvmAndNativeMain)
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.wasmjs)
                implementation(libs.ktor.client.js)
            }
        }
    }
}

// Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://github.com/gradle/gradle/issues/26091
//tasks.withType<AbstractPublishToMaven>().configureEach {
//    val signingTasks = tasks.withType<Sign>()
//    mustRunAfter(signingTasks)
//}

publishing {
    val mavenRepo = "https://maven.pkg.github.com/Sieveo/KMQTT"
    repositories {
        maven {
            url = uri(mavenRepo)
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

