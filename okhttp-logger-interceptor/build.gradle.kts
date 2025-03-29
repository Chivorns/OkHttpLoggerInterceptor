plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.chivorn.okhttp.logger.interceptor"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    compileOnly("com.google.code.gson:gson:2.10")
    compileOnly("com.jakewharton.timber:timber:5.0.1")
    compileOnly(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    compileOnly("com.squareup.okhttp3:okhttp")
    compileOnly("com.squareup.retrofit2:retrofit:2.9.0")
}

ext {
    this["PUBLISH_GROUP_ID"] = "com.github.chivorns"
    this["PUBLISH_VERSION"] = "1.4.2"
    this["PUBLISH_ARTIFACT_ID"] = "okhttp-logger-interceptor"
    this["PUBLISH_DESCRIPTION"] = "OkHttp Logger Interceptor"
    this["PUBLISH_URL"] = "https://github.com/Chivorns/okhttp-logger-interceptor"
    this["PUBLISH_LICENSE_NAME"] = "The Apache Software License, Version 2.0"
    this["PUBLISH_DEVELOPER_ID"] = "chivorns"
    this["PUBLISH_DEVELOPER_NAME"] = "Chivorn"
    this["PUBLISH_DEVELOPER_EMAIL"] = "chivorn@live.com"
    this["PUBLISH_SCM_CONNECTION"] = "scm:https://github.com/Chivorns/okhttp-logger-interceptor.git"
    this["PUBLISH_SCM_DEVELOPER_CONNECTION"] =
        "scm:https://github.com/Chivorns/okhttp-logger-interceptor.git"
    this["PUBLISH_SCM_URL"] = "https://github.com/Chivorns/okhttp-logger-interceptor"
}

apply(from = "https://raw.githubusercontent.com/Chivorns/maven-publish-plugin/refs/heads/main/publish-groovy.gradle")