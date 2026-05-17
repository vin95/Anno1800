import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.JavaExec

plugins {
    java
    application
}

fun loadDotEnv(filePath: java.io.File): Map<String, String> {
    if (!filePath.exists()) {
        return emptyMap()
    }

    return filePath.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val idx = line.indexOf('=')
            if (idx <= 0) {
                null
            } else {
                val key = line.substring(0, idx).trim()
                val value = line.substring(idx + 1).trim().trim('"')
                if (key.isEmpty()) null else key to value
            }
        }
        .toMap()
}

val dotEnv = loadDotEnv(file(".env"))
val sourceSets = the<SourceSetContainer>()

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

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.register<JavaExec>("debugGame") {
    group = "application"
    description = "Startet eine seeded Agent-Simulation fuer Debug-Zwecke. Seed/Spieler/Runden konfigurierbar in debug-game.ps1"
    dependsOn(tasks.named("classes"))
    mainClass.set("com.anno1800.debug.DebugGameRunner")
    classpath = sourceSets["main"].runtimeClasspath
    val cliArgs = findProperty("gameArgs")?.toString()?.split(" ") ?: listOf("42", "3", "200")
    args(cliArgs)
}

tasks.withType<JavaExec>().configureEach {
    dotEnv["JAVA_HOME"]?.let { javaHome ->
        val javaExecutable = file("$javaHome/bin/java.exe")
        if (javaExecutable.exists()) {
            executable = javaExecutable.absolutePath
        }
        environment("JAVA_HOME", javaHome)
    }
}

tasks.register<JavaExec>("simulation") {
    group = "application"
    description = "Runs dataset simulation using values from .env"
    dependsOn(tasks.named("classes"))

    mainClass.set("com.anno1800.simulation.DatasetGenerator")

    val envClasspath = dotEnv["SIMULATION_CLASSPATH"]
    classpath = if (!envClasspath.isNullOrBlank()) {
        val classpathEntries = envClasspath.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { file(it) }
        files(classpathEntries)
    } else {
        sourceSets["main"].runtimeClasspath
    }

    dotEnv["SIMULATION_OUTPUT"]?.let { args(it) }
}
