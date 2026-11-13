import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("java")

    kotlin("jvm") version "2.4.21"

    id("dev.clojurephant.clojure") version "0.9.1"

    id("org.jetbrains.intellij.platform") version "2.20.1"

    id("org.jetbrains.changelog") version "2.5.0"

    id("org.jetbrains.qodana") version "2026.2.2"

    id("org.jetbrains.kotlinx.kover") version "0.9.11"
}

kotlin {
    jvmToolchain(25)
}

java {
    toolchain {
        // IntelliJ 2026.3 platform classes are Java 25 bytecode, so the compilers must run on JDK 25
        languageVersion = JavaLanguageVersion.of(25)
    }
    // Keep the produced bytecode compatible with older IDE runtimes (JBR 21 in 2024.3+)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
    maven {
        name = "Clojars"
        url = uri("https://repo.clojars.org")
    }
    intellijPlatform {
        defaultRepositories()
    }
}


dependencies {
    implementation("org.clojure:clojure:1.12.6")
    implementation("http-kit:http-kit:2.8.1")

    intellijPlatform {
        intellijIdea("2026.3")
        bundledPlugins("org.jetbrains.kotlin", "com.intellij.java", "com.intellij.modules.jcef")
        plugins(
            "com.cursiveclojure.cursive:2026.4.1-263",
            "com.jetbrains.php:263.7307.200",
            "JavaScript:263.7307.200",
            "org.intellij.scala:2026.3.26",
            "org.jetbrains.plugins.go:263.7307.200",
            "PsiViewer:2026.2"
        )
        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        version

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild").get()
            untilBuild = providers.gradleProperty("pluginUntilBuild").get()
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = providers.gradleProperty("pluginVersion")
            .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            // Only the latest IntelliJ IDEA Community release is required.
            create(IntelliJPlatformType.IntellijIdea, "2026.3")
        }
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

//kover {
//    reports {
//        total {
//            xml {
//                onCheck = true
//            }
//        }
//    }
//}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
//qodana {
//    cachePath.set(file(".qodana").canonicalPath)
//    reportPath.set(file("build/reports/inspections").canonicalPath)
//    saveReport.set(true)
//    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
//}

tasks.withType(KotlinCompile::class).all {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // For creation of default methods in interfaces
        freeCompilerArgs.add("-jvm-default=enable")
    }
}

tasks.register("saveClassPathToFile") {
    doFirst {
        File("classpath.txt").writeText(sourceSets["main"].runtimeClasspath.asPath)
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    checkClojure {
        isEnabled = false
    }

    compileClojure {
        dependsOn(compileKotlin)
//        to get all output paths use: sourceSets.main.get().output.asPath
        classpath.from(sourceSets.main.get().kotlin.destinationDirectory.get())
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }

    buildSearchableOptions {
        enabled = false
    }
}

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                    )
                }
            }

            plugins {
                robotServerPlugin()
            }
        }
    }
}

clojure.builds.named("main") {
    aotAll()
    checkAll()
    reflection.set("fail")
}
