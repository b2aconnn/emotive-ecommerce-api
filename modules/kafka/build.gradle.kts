//plugins {
//    `java-library`
//    `java-test-fixtures`
//}

plugins {
    val kotlinVersion = "2.0.20"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.kapt") version kotlinVersion
    id("java-library")
    id("java-test-fixtures")
}

dependencies {
    api("org.springframework.kafka:spring-kafka")

    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:kafka")

    testFixturesImplementation("org.testcontainers:kafka")
}
