import org.gradle.api.tasks.JavaExec
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("tasks.ts.modifier")
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.js.plain.objects)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.axion.release)
}

group = "com.neo4j.importer.spec"

version = scmVersion.version

repositories { mavenCentral() }

kotlin {
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
        testRuns.named("test") { executionTask.configure { useJUnitPlatform() } }
    }
    js(IR) {
        binaries.library()
        compilations.named("main") {
            packageJson {
                name = "@neo4j-importer/graph-spec"
            }
        }
        nodejs {
            testTask {
                useMocha()
            }
        }
        compilerOptions {
            sourceMap = true
            moduleKind = JsModuleKind.MODULE_ES
            freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalWasmJsInterop")
            freeCompilerArgs.add("-Xes-long-as-bigint")
        }
        generateTypeScriptDefinitions()
    }
    macosArm64 {
        binaries.sharedLib()
        binaries.staticLib { baseName = "graphdatamodel" }
    }
    linuxX64 {
        binaries.sharedLib()
        binaries.staticLib { baseName = "graphdatamodel" }
    }
    linuxArm64 {
        binaries.sharedLib()
        binaries.staticLib { baseName = "graphdatamodel" }
    }

    applyDefaultHierarchyTemplate()
    // Override target source sets for KMP
    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val bridge by creating {
            dependsOn(commonMain)
        }
        val bridgeTest by creating {
            dependsOn(commonTest)
        }
        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
            compilations.getByName("main").defaultSourceSet.dependsOn(bridge)
            compilations.getByName("test").defaultSourceSet.dependsOn(bridgeTest)
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.schema)
            implementation(libs.kotlinx.serializer.json)
            implementation(libs.kotlinx.yamlkt)
            implementation(libs.kaseChange)
        }
        jsMain.dependencies {
            implementation(libs.kotlin.js.plain.objects)
            implementation(libs.kotlin.wrappers.ts)
        }
        commonTest.dependencies { implementation(libs.kotlin.test) }
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalJsExport")
        freeCompilerArgs.add("-opt-in=kotlin.js.ExperimentalJsStatic")
    }

    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.native.ExperimentalNativeApi")
    }
}

tasks.withType<Kotlin2JsCompile>().configureEach {
    compilerOptions {
        target = "es2015"
    }
}

val copyReadmeToJs by tasks.registering(Copy::class) {
    from(rootProject.file("README.md"))
    into(layout.buildDirectory.dir("dist/js/productionLibrary"))
}

/*
    Kotlin/JS doesn't support TypeScript unions
    https://youtrack.jetbrains.com/issue/KT-55101/
    This script modifies the generated types and generates a string union given a basic enum.
    It's a somewhat brittle hack but the type safety is much preferred on the frontend.
    There's the potential to use a different library for TS generation in the future which does support this natively.
 */
tasks.register("generateTsUnions", TypeScriptModifierTask::class.java) {
    dependsOn(copyReadmeToJs)
    typescriptFile =
        layout.buildDirectory
            .dir("dist/js/productionLibrary/")
            .get()
            .file("graph-spec.d.mts")
            .asFile
}

tasks.named("jsProductionLibraryCompileSync") {
    finalizedBy(copyReadmeToJs)
}

tasks.named("jsNodeProductionLibraryDistribution") {
    finalizedBy("generateTsUnions")
}

tasks.register<JavaExec>("generateGraphModelJsonSchema") {
    description = "Writes JSON Schema for GraphModel Go type generation"
    val compilation = kotlin.jvm().compilations.getByName("main")
    dependsOn(compilation.compileTaskProvider)
    classpath = compilation.output.classesDirs + compilation.compileDependencyFiles
    mainClass.set("schema.GenerateGraphModelJsonSchemaKt")
    workingDir = layout.projectDirectory.asFile
    doFirst {
        val outputFile = layout.projectDirectory.file("go/spec.json").asFile
        outputFile.parentFile.mkdirs()
        args(outputFile.absolutePath)
    }
}

scmVersion {
    versionCreator("versionWithBranch")
    tag { prefix.set("graph-spec") }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom {
                name = "graph-spec"
                description = "Uniform Graph Specification Library for Neo4j"
                url = "https://github.com/neo4j/import-spec"
                inceptionYear = "2024"
                organization {
                    name = "Neo4j, Neo4j Sweden AB"
                    url = "https://neo4j.com"
                }
                licenses {
                    license {
                        name = "Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "manual"
                    }
                }
                developers {
                    developer {
                        id = "team-data-importer"
                        name = "Data Importer Team"
                        organization = "Neo4j"
                        organizationUrl = "https://neo4j.com"
                    }
                    developer {
                        id = "team-connectors"
                        name = "Connectors Team"
                        organization = "Neo4j"
                        organizationUrl = "https://neo4j.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/neo4j/import-spec.git"
                    developerConnection = "scm:git:git@github.com:neo4j/import-spec.git"
                    url = "https://github.com/neo4j/import-spec"
                }
            }
        }
    }
    repositories {
        maven {
            name = "mavenCentral"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().contains("alpha")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = System.getenv("SONATYPE_USERNAME") ?: project.providers.gradleProperty("sonatypeUsername").orNull
                password = System.getenv("SONATYPE_PASSWORD") ?: project.providers.gradleProperty("sonatypePassword").orNull
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_KEY") ?: project.providers.gradleProperty("signingKey").orNull
    val signingPassword = System.getenv("SIGNING_PASSWORD") ?: project.providers.gradleProperty("signingPassword").orNull

    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        ktlint().editorConfigOverride(
            mapOf("code_style" to "intellij_idea")
        )
        endWithNewline()
        licenseHeaderFile(rootProject.file("license-header.txt"))
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
        endWithNewline()
    }
    kotlin {
        target(
            project.fileTree("src/commonMain/kotlin"),
            project.fileTree("src/commonTest/kotlin"),
            project.fileTree("src/jsMain/kotlin"),
            project.fileTree("src/bridge/kotlin"),
            project.fileTree("src/bridgeTest/kotlin")
        )
    }
}
