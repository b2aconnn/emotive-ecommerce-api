//plugins {
//    id("org.jetbrains.kotlin.plugin.jpa")
//}

plugins {
    val kotlinVersion = "2.0.20"

    id("org.jetbrains.kotlin.jvm") version(kotlinVersion)
    id("org.jetbrains.kotlin.kapt") version(kotlinVersion)
    id("org.jetbrains.kotlin.plugin.spring") version(kotlinVersion)
    id("org.jetbrains.kotlin.plugin.jpa") version(kotlinVersion)
}

dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":modules:redis"))
    implementation(project(":modules:kafka"))
    implementation(project(":supports:jackson"))
    implementation(project(":supports:logging"))
    implementation(project(":supports:monitoring"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // querydsl
    kapt("com.querydsl:querydsl-apt::jakarta")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
    testImplementation(testFixtures(project(":modules:redis")))
}
