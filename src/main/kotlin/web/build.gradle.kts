plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    implementation("io.ktor:ktor-server-core-jvm:2.3.5")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.5")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.5")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.5")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.5")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.5")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.5")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.5")

    implementation("io.ktor:ktor-server-openapi:2.3.5")
    implementation("io.ktor:ktor-server-swagger-jvm:2.3.5")

    implementation("ch.qos.logback:logback-classic:1.4.8")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
}