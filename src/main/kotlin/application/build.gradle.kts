plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":infrastructure"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("at.favre.lib:bcrypt:0.9.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
}