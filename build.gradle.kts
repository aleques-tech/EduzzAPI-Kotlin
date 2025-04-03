plugins {
    kotlin("jvm")
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
    compileOnly("io.vertx:vertx-web-client:_")
    compileOnly("io.vertx:vertx-lang-kotlin-coroutines:_")
    compileOnly(KotlinX.serialization.json)
    compileOnly(KotlinX.serialization.core)
    compileOnly(KotlinX.coroutines.core)
    
    testImplementation(kotlin("test"))
    testImplementation("io.github.cdimascio:dotenv-kotlin:_")
    testImplementation(KotlinX.serialization.json)
    testImplementation(KotlinX.coroutines.core)
    testImplementation("io.vertx:vertx-web-client:_")
    testImplementation("io.vertx:vertx-lang-kotlin-coroutines:_")
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
    
    sourceSets {
        main {
            kotlin.srcDirs("src/main/kotlin")
        }
    }
    
    // Explicitly export serializers package
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "com.aleques.eduzzApi",
            "Export-Package" to "com.aleques.eduzzApi"
        )
    }
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
