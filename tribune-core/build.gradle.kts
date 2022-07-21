plugins {
   id("java")
   kotlin("multiplatform")
   id("java-library")
   id("org.jetbrains.kotlinx.kover") version "0.5.1"
}

repositories {
   mavenCentral()
}

kotlin {

   targets {

      jvm {
         compilations.all {
            kotlinOptions {
               jvmTarget = "11"
            }
         }
      }

      js(IR) {
         browser()
         nodejs()
      }

      linuxX64()

      mingwX64()

      macosX64()
      macosArm64()

      tvos()
      tvosSimulatorArm64()

      watchosArm32()
      watchosArm64()
      watchosX86()
      watchosX64()
      watchosSimulatorArm64()

      iosX64()
      iosArm64()
      iosArm32()
      iosSimulatorArm64()
   }

   sourceSets {

      val commonMain by getting {
         dependencies {
            api("io.arrow-kt:arrow-core:_")
         }
      }

      val jvmMain by getting {
         dependsOn(commonMain)
         dependencies {
         }
      }

      val jvmTest by getting {
         dependsOn(jvmMain)
         dependencies {
            implementation(Testing.kotest.assertions.core)
            implementation(Testing.kotest.runner.junit5)
         }
      }

      all {
         languageSettings.optIn("kotlin.OverloadResolutionByLambdaReturnType")
      }
   }
}

tasks.named<Test>("jvmTest") {
   useJUnitPlatform()
   filter {
      isFailOnNoMatchingTests = false
   }
   testLogging {
      showExceptions = true
      showStandardStreams = true
      events = setOf(
         org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
         org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
      )
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
   }
   extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
      isDisabled = false
      includes = listOf("com.sksamuel.*")
      excludes = emptyList()
   }
}

apply(from = "../publish-mpp.gradle.kts")
