plugins {
    kotlin("jvm") version "1.9.20-Beta"
    kotlin("plugin.serialization")
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
}

kotlin {
    jvmToolchain(11)
}

/*
application {
    mainClass.set("MainKt")
}*/
