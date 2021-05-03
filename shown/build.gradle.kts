plugins {
    id("com.android.library")
    kotlin("android")
}

val composeVersion = property("composeVersion") as String

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 21
        targetSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildFeatures {
        buildConfig = false
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    composeOptions {
        version = composeVersion
        kotlinCompilerExtensionVersion = composeVersion
    }
    packagingOptions {
        resources {
            pickFirsts += "META-INF/AL2.0"
            pickFirsts += "META-INF/LGPL2.1"
        }
    }
}

dependencies {
    api("androidx.compose.ui:ui:${composeVersion}")
    api("androidx.compose.foundation:foundation:${composeVersion}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("com.willowtreeapps.assertk:assertk-jvm:0.23.1")
    androidTestImplementation("androidx.activity:activity-compose:1.3.0-alpha07")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${composeVersion}")
}
