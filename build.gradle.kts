plugins {
    id("java")
    id("com.diffplug.spotless") version "7.0.3"
}

group = "com.github.huarngpa"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

spotless {
    java {
        googleJavaFormat()
        target("src/*/java/**/*.java")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("compileJava") {
    dependsOn("spotlessApply")
}