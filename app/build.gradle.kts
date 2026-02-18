import java.util.Properties
import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
}

// 使用新的 ApplicationExtension 替代旧的 android 扩展
configure<ApplicationExtension> {
    namespace = "com.careful.HyperFVM"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.careful.HyperFVM"
        minSdk = 31
        targetSdk = 36
        versionCode = 78
        versionName = "3.1.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 1. 读取local.properties文件
    val localProperties = Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            load(localPropertiesFile.inputStream())
        }
    }

    // 2. 配置签名信息（核心修改：仅创建release，修改默认debug而非新建）
    signingConfigs {
        // 创建release签名配置（自定义）
        create("release") {
            storeFile = file(localProperties.getProperty("keystore.path") ?: error("未配置keystore.path"))
            storePassword = localProperties.getProperty("keystore.password") ?: error("未配置keystore.password")
            keyAlias = localProperties.getProperty("keystore.alias") ?: error("未配置keystore.alias")
            keyPassword = localProperties.getProperty("keystore.aliasPassword") ?: error("未配置keystore.aliasPassword")
        }

        // 修改默认的debug签名配置（而非新建），避免重复
        getByName("debug") {
            storeFile = file(localProperties.getProperty("keystore.path") ?: error("未配置keystore.path"))
            storePassword = localProperties.getProperty("keystore.password") ?: error("未配置keystore.password")
            keyAlias = localProperties.getProperty("keystore.alias") ?: error("未配置keystore.alias")
            keyPassword = localProperties.getProperty("keystore.aliasPassword") ?: error("未配置keystore.aliasPassword")
        }
    }

    // 3. 合并重复的buildTypes块（核心修改：一个块内配置所有buildType逻辑）
    buildTypes {
        release {
            // 关联release签名
            signingConfig = signingConfigs.getByName("release")
            // 混淆配置
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // 关联修改后的默认debug签名
            signingConfig = signingConfigs.getByName("debug")
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
    implementation(libs.androidx.biometric)
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

    //Glide库，用来加载图片
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
    implementation("io.noties.markwon:image:4.6.2")
    implementation("io.noties.markwon:image-glide:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")

    //Blur
    implementation(libs.blurview)
}