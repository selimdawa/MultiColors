plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech)
}

android {
    namespace = "io.selimdawa.multicolors"
    compileSdk = 37

    defaultConfig {
        minSdk = 24

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
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

mavenPublishing {
    coordinates(groupId = "io.github.selimdawa", artifactId = "multi-colors", version = "1.0.7-beta")

    // publishToMavenCentral(automaticRelease = true)

    // if (!System.getenv("JITPACK").isNullOrEmpty()) {
    //     // Skip signing on JitPack
    // } else {
    //     signAllPublications()
    // }

    pom {
        name.set("Multi Colors")
        description.set("A professional, reactive theme management library for Android with support for colors and gradients.")

        url.set("https://github.com/selimdawa/MultiColors")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("selimdawa")
                name.set("Selim Dawa")
                email.set("selimdawa@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/selimdawa/MultiColors")
            connection.set("scm:git:https://github.com/selimdawa/MultiColors.git")
            developerConnection.set("scm:git:ssh://git@github.com:selimdawa/MultiColors.git")
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.flexbox)
}