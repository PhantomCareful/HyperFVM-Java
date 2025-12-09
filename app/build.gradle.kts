
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.careful.HyperFVM"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.careful.HyperFVM"
        minSdk = 31
        targetSdk = 36
        versionCode = 39
        versionName = "2.1.3"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    dataBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.androidx.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //做底部导航栏用的
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.fragment)

    //动态取色用的
    implementation(libs.material.v1120)

    //做卡片布局用的
    implementation(libs.androidx.cardview)

    implementation(libs.glide)

    //解析Http网页用的
    implementation(libs.okhttp)

    //HTML解析库
    implementation(libs.jsoup)

    //CSV解析库
    implementation(libs.opencsv)

    //后台任务需要用的
    implementation(libs.androidx.work.runtime)

    //支持显示MarkDown笔记
    implementation(libs.core)
    implementation(libs.ext.tables)
    implementation(libs.ext.strikethrough)

    //Blur
    implementation(libs.blurview)
}