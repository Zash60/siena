plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.mozilla.rust-android-gradle.rust-android") }
android {
    namespace = "com.example.siena"
    compileSdk = 34
    defaultConfig { applicationId = "com.example.siena"; minSdk = 26; targetSdk = 34; versionCode = 1; versionName = "1.0"; ndk { abiFilters.add("arm64-v8a"); abiFilters.add("x86_64") } }
    buildFeatures { viewBinding = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_1_8; targetCompatibility = JavaVersion.VERSION_1_8 }
    kotlinOptions { jvmTarget = "1.8" }
}
cargo { module = "../../"; libname = "siena"; targets = listOf("arm64", "x86_64"); profile = "release"; pythonCommand = "python3" }
dependencies { implementation("androidx.core:core-ktx:1.9.0"); implementation("androidx.appcompat:appcompat:1.6.1"); implementation("com.google.android.material:material:1.9.0"); implementation("androidx.constraintlayout:constraintlayout:2.1.4"); implementation("androidx.activity:activity-ktx:1.7.2") }
