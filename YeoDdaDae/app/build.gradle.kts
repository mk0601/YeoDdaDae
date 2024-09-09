plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.bucheon.yeoddadae"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bucheon.yeoddadae"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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

    dataBinding {
        isEnabled = true
    }

    configurations {
        implementation {
            exclude(module = "protobuf-java")
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(files("libs\\com.skt.Tmap_1.75.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // 달력 라이브러리
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.1.1")

    // 파이어베이스
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // Compose 버전 정의
    val compose_version = "1.3.1"
    val compose_ui_version = "1.3.3"

    // TMapUISDK
    implementation("com.tmapmobility.tmap:tmap-ui-sdk:1.0.0.0068")

    // VSM SDK
    implementation("com.google.flatbuffers:flatbuffers-java:1.11.0")

    // Navi SDK 의존성
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.google.android.exoplayer:exoplayer:2.17.1")
    implementation("com.google.android.exoplayer:exoplayer-core:2.17.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.17.1")

    // UI SDK 의존성
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("androidx.compose.ui:ui:$compose_ui_version")
    implementation("androidx.compose.foundation:foundation:$compose_version")
    implementation("androidx.compose.material:material:$compose_version")
    implementation("androidx.compose.material:material-icons-core:$compose_version")
    implementation("androidx.compose.material:material-icons-extended:$compose_version")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("com.github.bumptech.glide:glide:4.13.2")
    implementation("com.google.android.gms:play-services-location:17.0.0")
    implementation("com.airbnb.android:lottie:3.0.7")
    
    // 하단 네비게이션바 종속성
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("androidx.navigation:navigation-fragment:2.3.5")
    implementation ("androidx.navigation:navigation-ui:2.3.5")
}