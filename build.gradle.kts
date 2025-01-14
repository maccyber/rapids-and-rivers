import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.6.7"
val kafkaVersion = "3.1.0"
val micrometerRegistryPrometheusVersion = "1.8.1"
val junitJupiterVersion = "5.8.2"
val jacksonVersion = "2.13.2"

group = "com.github.navikt"
version = properties["version"] ?: "local-build"

plugins {
    kotlin("jvm") version "1.6.0"
    id("java")
    id("maven-publish")
}

dependencies {
    api("ch.qos.logback:logback-classic:1.3.0-alpha10")
    api("net.logstash.logback:logstash-logback-encoder:7.0.1") {
        exclude("com.fasterxml.jackson.core")
    }
    api("io.ktor:ktor-server-cio:$ktorVersion")

    api("org.apache.kafka:kafka-clients:$kafkaVersion")

    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    api("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    api("io.micrometer:micrometer-registry-prometheus:$micrometerRegistryPrometheusVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

    testImplementation("com.github.navikt:kafka-embedded-env:kafka310-SNAPSHOT")
    testImplementation("org.awaitility:awaitility:4.1.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
        showExceptions = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.4"
}

repositories {
    maven("https://jitpack.io")
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

val githubUser: String? by project
val githubPassword: String? by project

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/rapids-and-rivers")
            credentials {
                username = githubUser
                password = githubPassword
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {

            pom {
                name.set("rapids-rivers")
                description.set("Rapids and Rivers")
                url.set("https://github.com/navikt/rapids-and-rivers")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/rapids-and-rivers.git")
                    developerConnection.set("scm:git:https://github.com/navikt/rapids-and-rivers.git")
                    url.set("https://github.com/navikt/rapids-and-rivers")
                }
            }
            from(components["java"])
        }
    }
}
