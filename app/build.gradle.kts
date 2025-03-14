/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("io.appmetrica.analytics")
    id("jacoco")
    id("com.google.gms.google-services")
}

val keystorePropertiesFile = rootProject.file("/signing.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists())
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))

val appMetricaPropertiesFile = rootProject.file("/appMetrica.properties")
val appMetricaProperties = Properties()
if (appMetricaPropertiesFile.exists()){
    appMetricaProperties.load(FileInputStream(appMetricaPropertiesFile))

    appmetrica {
        setPostApiKey(appMetricaProperties["apiKey"].toString())
        enableAnalytics = true
    }
}


android {
    namespace = "com.ghostwalker18.schedule"
    compileSdk = 34

    bundle {
        language {
            enableSplit = false
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["keystore"].toString())
            storePassword = keystoreProperties["keystorePassword"].toString()
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
        }
    }

    defaultConfig {
        applicationId = "com.ghostwalker18.schedule"
        minSdk = 26
        targetSdk = 34
        versionCode = 14
        versionName = "4.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = false
            isDebuggable = true
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.room:room-guava:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.guava:listenablefuture:1.0")
    implementation("com.google.guava:guava:33.4.0-android")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("org.jsoup:jsoup:1.12.2")
    implementation("com.github.pjfanning:excel-streaming-reader:5.0.2")
    implementation("org.apache.xmlbeans:xmlbeans:3.1.0")
    implementation("javax.xml.stream:stax-api:1.0")
    implementation("com.fasterxml:aalto-xml:1.2.2")
    implementation("io.appmetrica.analytics:analytics:7.5.0")
    implementation("ru.rustore.sdk:universalpush:6.5.0")
    implementation("ru.rustore.sdk:universalrustore:6.5.0")
    implementation("ru.rustore.sdk:universalfcm:6.5.0")
    implementation("com.google.firebase:firebase-messaging:22.0.0")
    implementation("com.google.android.gms:play-services-base:17.5.0")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("org.mockito:mockito-core:4.11.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}