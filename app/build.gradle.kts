plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.prodkilly.bachewatch"

    // Cambia el bloque compileSdk viejo por esta línea directa:
    compileSdk = 35

    defaultConfig {
        applicationId = "com.prodkilly.bachewatch"

        // Si quieres que tu app corra en más dispositivos, podrías bajar esto a 26.
        // Si necesitas forzosamente Android 12 como mínimo, déjalo en 31.
        minSdk = 31

        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ... el resto de tu archivo (buildTypes, compileOptions, etc.) queda igual

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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Integración de ViewModel con Compose (Arregla 'viewModel')
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    // Soporte para LiveData en Compose (Arregla 'observeAsState')
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")
    // Plataforma de Firebase (BOM) para gestionar versiones automáticamente
    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))

    // Dependencias de Firebase requeridas para BacheWatch
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore") // Base de datos
    implementation("com.google.firebase:firebase-storage")   // Almacenamiento de fotos
    implementation("com.google.firebase:firebase-auth")      // Identificación de dispositivos

    // Iconos extendidos de Material 3 (Arregla 'Icons.Filled.Add' e 'Icons.Outlined.LocationOn')
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)


}