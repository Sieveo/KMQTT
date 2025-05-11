
allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.27.0")
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.goncalossilva.resources)
}

subprojects {
    group = "com.sieveo.kmqtt"
    version = "2.0.0-SNAPSHOT"
}
