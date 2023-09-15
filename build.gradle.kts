plugins {
    kotlin("jvm") version "1.9.20-Beta"
    kotlin("plugin.serialization")
    id("pl.allegro.tech.build.axion-release")
    `java-library`
    `maven-publish`
}

group = "com.aleques"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //implementation(platform("org.jetbrains.kotlin:kotlin-bom:_"))
    compileOnly(Square.okHttp3.okHttp)
    implementation(Square.retrofit2)
    implementation(JakeWharton.retrofit2.converter.kotlinxSerialization)
    implementation(Square.okHttp3.loggingInterceptor)

    implementation(KotlinX.serialization.json)
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
