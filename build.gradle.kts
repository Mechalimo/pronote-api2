import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.util.*

plugins {
    `maven-publish`
    signing
    kotlin("multiplatform") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.misterassm"
version = "0.3.2"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()

        compilations.all {
            kotlinOptions.jvmTarget = "17" // 🔥 changé de 1.8 à 17
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
            compileKotlinTask.kotlinOptions.freeCompilerArgs += listOf("-Xerror-tolerance-policy=SEMANTIC")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                compileOnly("io.ktor:ktor-client-core:2.3.12")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.12")
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

val jvmShadowJar by tasks.named("jvmShadowJar")

publishing {
    publications.withType<MavenPublication> {
        artifact(jvmShadowJar)
        artifact(javadocJar.get())

        pom {
            name.set("Kronote")
            description.set("Library to easily retrieve information from a Pronote server (Index-Education) for JVM/JS")
            url.set("https://github.com/MisterAssm/pronote-api")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("MisterAssm")
                    name.set("Assim ZEMOUCHI")
                    email.set("assim.zpr@gmail.com")
                }
            }
            scm {
                url.set("https://github.com/MisterAssm/pronote-api")
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

// ✅ Désactive les tâches Kotlin/JS lock qui bloquent le build en Docker
tasks.matching { it.name == "kotlinStoreYarnLock" || it.name == "kotlinUpgradePackageLock" }.configureEach {
    enabled = false
}
