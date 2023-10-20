import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.16.0"
    id("jvm-test-suite")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.junit.api)
    testRuntimeOnly(libs.bundles.junit.runtime)
}

intellij {
    // version.set("233-EAP-SNAPSHOT")
    version.set("2023.2.2")

    type.set("IC")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            targets {
                all {
                    testTask.configure {
                        useJUnitPlatform() {
                            excludeEngines(
                                "junit-vintage",
                                "junit-jupiter",
                                "junit-platform-suite", // suite shouldn't be launched
                            )
                        }
                    }
                }
            }
        }

        val junit4test by registering(JvmTestSuite::class) {
            sources {
                kotlin {
                    srcDir(sourceSets[test.name].kotlin.srcDirs)
                }
            }
            testType = "junit4-unit-test"
            targets {
                all {
                    testTask.configure {
                        useJUnitPlatform {
                            includeEngines("junit-vintage")
                        }
                        doLast {
                            println("Test task: " + testTask.get().name)
                        }
                        shouldRunAfter(test)
                    }
                }
            }
        }

        val junit5test by registering(JvmTestSuite::class) {
            sources {
                kotlin {
                    srcDir(sourceSets[test.name].kotlin.srcDirs)
                }
            }
            testType = "junit5-unit-test"
            targets {
                all {
                    testTask.configure {
                        useJUnitPlatform {
                            includeEngines("junit-jupiter")
                        }
                        doLast {
                            println("Test task: " + testTask.get().name)
                        }
                        shouldRunAfter(test)
                    }
                }
            }
        }

        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter(libs.versions.junit.jupiter.get())

            dependencies {
                implementation.bundle(libs.bundles.assertj)
                implementation.bundle(libs.bundles.junit.api)
                runtimeOnly.bundle(libs.bundles.junit.runtime)
            }

            targets.configureEach {
                testTask.configure {
                    // useJUnitPlatform {
                    //     includeEngines("junit-vintage", "junit-jupiter")
                    // }

                    testLogging {
                        showStackTraces = true
                        exceptionFormat = TestExceptionFormat.FULL

                        events = setOf(
                            TestLogEvent.FAILED,
                            TestLogEvent.SKIPPED,
                        )
                    }

                    if (providers.environmentVariable("CI").isPresent) {
                        systemProperties(
                            "dd.service" to "datadog-intellij-plugin",
                            "dd.civisibility.enabled" to "true",
                            "dd.civisibility.agentless.enabled" to "true",
                            "dd.instrumentation.telemetry.enabled" to "false",
                        )
                    }
                }
            }
        }
    }
}