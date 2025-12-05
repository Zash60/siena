buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
        classpath("org.mozilla.rust-android-gradle:plugin:0.9.3")
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
