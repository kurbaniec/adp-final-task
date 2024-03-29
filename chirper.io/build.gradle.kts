plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "io.chirper"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

sourceSets {
    create("resilienceTest") {
        java.srcDir("src/resilience-test/java")
        resources.srcDir("src/resilience-test/resources")
        compileClasspath += main.get().output + sourceSets["test"].output
        runtimeClasspath += main.get().output + sourceSets["test"].output
        configurations["resilienceTestImplementation"]
            .extendsFrom(configurations["testImplementation"])
        configurations["resilienceTestRuntimeOnly"]
            .extendsFrom(configurations["testRuntimeOnly"])
        configurations["resilienceTestCompileOnly"]
            .extendsFrom(configurations["testCompileOnly"])
        configurations["resilienceTestAnnotationProcessor"]
            .extendsFrom(configurations["testAnnotationProcessor"])
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.github.resilience4j:resilience4j-spring-boot2:2.1.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.apache.tika:tika-core:2.7.0")

    runtimeOnly("com.h2database:h2")
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootRun {
    jvmArgs = listOf("-Dspring.output.ansi.enabled=ALWAYS")
}

val resilienceTest by tasks.registering(Test::class) {
    testClassesDirs = sourceSets["resilienceTest"].output.classesDirs
    classpath = sourceSets["resilienceTest"].runtimeClasspath
}