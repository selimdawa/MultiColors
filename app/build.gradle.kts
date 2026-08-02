plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.flatcode.multicolors"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.flatcode.multicolors"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":multicolors"))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.coil)
}