plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.evee"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.evee"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.cardview)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
