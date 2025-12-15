plugins {
    java
    jacoco
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "eu.petrvich.construction"
version = "1.0.0"

// dependencies
val testcontainersVersion: String by project
val testcontainersJUnitVersion: String by project
val springdocOpenapiStarterWebmvcUi: String by project

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // OpenAPI/Swagger documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocOpenapiStarterWebmvcUi}")

    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Micrometer for metrics
    implementation("io.micrometer:micrometer-core")

    implementation("com.github.ben-manes.caffeine:caffeine")

    // Lombok for reducing boilerplate
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.testcontainers:testcontainers:${testcontainersVersion}")
    testImplementation("org.testcontainers:junit-jupiter:${testcontainersJUnitVersion}")

    // Test Lombok
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // Generate coverage report after tests
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Tests are required to run before generating the report
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% code coverage minimum
            }
        }
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("construction-planner-${version}.jar")
}
