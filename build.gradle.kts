plugins {
    java
    application
}

group = "com.anno1800"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Testing dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.anno1800.ui.TerminalGameUI")
}

tasks.test {
    useJUnitPlatform()
}
