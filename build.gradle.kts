plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization")
    id("pl.allegro.tech.build.axion-release")
    `java-library`
    `maven-publish`
}

group = "com.aleques"
project.version = scmVersion.version

repositories {
    mavenCentral()
}

// Configure system properties for secure XML processing
tasks.withType<JavaExec> {
    systemProperty("javax.xml.accessExternalDTD", "all")
    systemProperty("javax.xml.accessExternalSchema", "all")
}

dependencies {
    compileOnly("io.vertx:vertx-web-client:4.5.7")
    compileOnly("io.vertx:vertx-lang-kotlin-coroutines:4.5.7")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    compileOnly(KotlinX.coroutines.core)
    
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    systemProperties["junit.jupiter.execution.parallel.enabled"] = true
    systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
    systemProperties["javax.xml.accessExternalDTD"] = "all"
    systemProperties["javax.xml.accessExternalSchema"] = "all"
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    jvmArgs = listOf("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
        }
    }
}
