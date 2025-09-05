import java.io.FileOutputStream
import java.security.KeyStore
import java.util.Properties
import java.io.File

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("kotlin-kapt")
  id("com.google.dagger.hilt.android")
}

android {
  namespace = "com.wes.truthdare"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.wes.truthdare"
    minSdk = 28
    targetSdk = 34
    versionCode = 1
    versionName = "1.0.0"
    vectorDrawables.useSupportLibrary = true
  }

  // Generate test keystore for signing
  val keystoreFile = File("${project.buildDir}/keystore/release.keystore")
  val keystoreProperties = File("${project.buildDir}/keystore/keystore.properties")
  
  tasks.register("generateKeystore") {
    doLast {
      if (!keystoreFile.exists()) {
        keystoreFile.parentFile.mkdirs()
        
        // Generate keystore
        exec {
          commandLine(
            "keytool", "-genkey", "-v",
            "-keystore", keystoreFile.absolutePath,
            "-alias", "truthdare",
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-storepass", "truthdare123",
            "-keypass", "truthdare123",
            "-dname", "CN=TruthDare, OU=Dev, O=WES, L=Amsterdam, S=NH, C=NL"
          )
        }
        
        // Create properties file
        val props = Properties()
        props.setProperty("storeFile", keystoreFile.absolutePath)
        props.setProperty("storePassword", "truthdare123")
        props.setProperty("keyAlias", "truthdare")
        props.setProperty("keyPassword", "truthdare123")
        
        keystoreProperties.parentFile.mkdirs()
        FileOutputStream(keystoreProperties).use { 
          props.store(it, "Keystore properties for Truth or Dare Deluxe")
        }
        
        println("Keystore and properties file generated successfully")
      } else {
        println("Keystore already exists, skipping generation")
      }
    }
  }

  // Load signing config from properties
  signingConfigs {
    create("release") {
      if (keystoreFile.exists() && keystoreProperties.exists()) {
        val props = Properties()
        props.load(keystoreProperties.inputStream())
        
        storeFile = file(props.getProperty("storeFile"))
        storePassword = props.getProperty("storePassword")
        keyAlias = props.getProperty("keyAlias")
        keyPassword = props.getProperty("keyPassword")
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
    debug { 
      isMinifyEnabled = false 
    }
  }

  buildFeatures { 
    compose = true 
  }
  
  composeOptions { 
    kotlinCompilerExtensionVersion = "1.5.14" 
  }
  
  packaging {
    resources.excludes += setOf(
      "META-INF/*", "kotlin/**", "okhttp3/**", "kotlinx/**"
    )
  }

  // Koppel vraaggenerator aan preBuild
  applicationVariants.all {
    preBuildProvider.configure {
      dependsOn("generateQuestionPacks")
    }
  }
}

dependencies {
  implementation(platform("androidx.compose:compose-bom:2024.06.00"))
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3:1.2.1")
  debugImplementation("androidx.compose.ui:ui-tooling")

  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
  implementation("androidx.navigation:navigation-compose:2.8.0")

  implementation("com.google.dagger:hilt-android:2.51")
  kapt("com.google.dagger:hilt-compiler:2.51")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  kapt("androidx.room:room-compiler:2.6.1")

  implementation("androidx.datastore:datastore-preferences:1.1.1")
  implementation("androidx.security:security-crypto:1.1.0-alpha06")

  // Vosk als lokale AAR – geen netwerk
  implementation(files("libs/vosk-android.aar"))
  
  // Testing
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.mockito:mockito-core:5.10.0")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
  testImplementation("androidx.arch.core:core-testing:2.2.0")
}

// Add generateKeystore task to preBuild
tasks.named("preBuild") {
  dependsOn("generateKeystore")
}

// Question Generator Task
tasks.register("generateQuestionPacks") {
  group = "buildsetup"
  doLast {
    val outDir = file("$projectDir/src/main/assets/questions").apply { mkdirs() }
    
    // We'll implement the question generation logic in a separate class
    // that will be called from here
    println("Generating question packs...")
    
    // Import and use the QuestionTemplateEngine
    val questionGenerator = com.wes.truthdare.buildtasks.QuestionTemplateEngine()
    questionGenerator.generateAllQuestionPacks(outDir)
    
    println("Question packs generated successfully")
  }
}