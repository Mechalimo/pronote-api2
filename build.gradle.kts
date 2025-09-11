import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.MisterAssm"
version = "0.3.2"

repositories {
    mavenCentral()
}

val kotlinxSerializationVersion = "1.7.3"
val kotlinxDatetimeVersion = "0.6.1"
val ktorVersion = "3.0.0"

kotlin {
    jvm {
        withJava()
        compilations.all {
            kotlinOptions.jvmTarget = "17" // Updated to match your JDK 17
            kotlinOptions.freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjsr305=strict"
            )
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        nodejs()
        compilations.all {
            // Fixed deprecated access to compileKotlinTask
            kotlinOptions.freeCompilerArgs += listOf("-Xerror-tolerance-policy=SEMANTIC")
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    // Renamed to avoid naming conflict with default hierarchy template
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("nativeTarget")
        hostOs == "Linux" -> linuxX64("nativeTarget")
        isMingwX64 -> mingwX64("nativeTarget")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
                // Changed from compileOnly to implementation for native compatibility
                implementation("io.ktor:ktor-client-core:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(npm("aes-js", "3.1.2"))
                implementation(npm("md5", "2.3.0"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeTargetMain by getting
        val nativeTargetTest by getting
    }
}

fun registerShadowJar(targetName: String) {
    kotlin.targets.named<KotlinJvmTarget>(targetName) {
        compilations.named("main") {
            tasks {
                val shadowJar = register<ShadowJar>("${targetName}ShadowJar") {
                    group = "build"
                    from(output)
                    configurations = listOf(runtimeDependencyFiles)
                    archiveAppendix.set(targetName)
                    archiveClassifier.set("all")
                    mergeServiceFiles()
                }
                getByName("${targetName}Jar") {
                    finalizedBy(shadowJar)
                }
            }
        }
    }
}

registerShadowJar("jvm")

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}