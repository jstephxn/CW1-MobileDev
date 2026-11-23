import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cw1_trafficapplication_s2534002"

    compileSdk {
        version = release(37)
    }
    buildFeatures {
        buildConfig =  true
    }
    defaultConfig {
        applicationId = "com.example.cw1_trafficapplication_s2534002"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "TRAFFIC_CLIENT_ID",
            "\"${localProperties.getProperty("TRAFFIC_CLIENT_ID", "")}\""
        )

        buildConfigField(
            "String",
            "TRAFFIC_CLIENT_KEY",
            "\"${localProperties.getProperty("TRAFFIC_CLIENT_KEY", "")}\""
        )

        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY", "")

    }



    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    implementation("com.google.android.gms:play-services-maps:19.2.0")
}