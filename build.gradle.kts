plugins {
    kotlin("jvm") version "1.9.20-Beta"
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

dependencies {
    //implementation(platform("org.jetbrains.kotlin:kotlin-bom:_"))
    implementation("io.vertx:vertx-web-client:4.5.7")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.5.7")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation(KotlinX.coroutines.core)
    implementation("io.github.cdimascio:dotenv-kotlin:_")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    systemProperties["junit.jupiter.execution.parallel.enabled"] = true
    systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    jvmArgs = listOf("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")
}

kotlin {
    jvmToolchain(11)
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
