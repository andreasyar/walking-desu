import java.time.Duration
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

plugins {
    application
}

dependencies {
    implementation(project(":common"))
}

application {
    mainClass = "wand6.server.JavaServer"
    applicationName = "wand6-server"
}

val executableJar = tasks.register<Jar>("executableJar") {
    group = "build"
    description = "Build a standalone server JAR, including its runtime dependencies."
    archiveBaseName = "wand6-server"
    archiveClassifier = "standalone"
    manifest.attributes["Main-Class"] = application.mainClass.get()
    from(sourceSets.main.get().output)
    val runtime = configurations.runtimeClasspath
    dependsOn(runtime)
    from({ runtime.get().map { if (it.isDirectory) it else zipTree(it) } }) {
        exclude("META-INF/MANIFEST.MF")
    }
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

tasks.assemble {
    dependsOn(executableJar)
}

val checks = sourceSets.create("checks") {
    compileClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}
val networkSmoke = tasks.register<JavaExec>("networkSmoke") {
    group = "verification"
    description = "Check HELLO/NAME, map and the authoritative movement on an isolated server."
    dependsOn(executableJar)
    classpath = checks.runtimeClasspath
    mainClass = "wand6.server.NetworkSmokeCheck"
    args(executableJar.get().archiveFile.get().asFile.absolutePath)
    timeout = Duration.ofSeconds(45)
}
tasks.check { dependsOn(networkSmoke) }

val mapCheck = tasks.register<JavaExec>("mapCheck") {
    group = "verification"
    classpath = checks.runtimeClasspath
    mainClass = "wand6.server.MapCheck"
}
tasks.check { dependsOn(mapCheck) }

val multiplayerCheck = tasks.register<JavaExec>("multiplayerCheck") {
    group = "verification"
    description = "Check visibility, delivery and connection cleanup with real network peers."
    classpath = checks.runtimeClasspath
    mainClass = "wand6.server.MultiplayerCheck"
    timeout = Duration.ofSeconds(45)
}
tasks.check { dependsOn(multiplayerCheck) }

val combatCheck = tasks.register<JavaExec>("combatCheck") {
    group = "verification"
    description = "Check authoritative PvP, flights, death/respawn and cancellation over real streams."
    classpath = checks.runtimeClasspath
    mainClass = "wand6.server.CombatCheck"
    timeout = Duration.ofSeconds(45)
}
tasks.check { dependsOn(combatCheck) }

val towerDefenceCheck = tasks.register<JavaExec>("towerDefenceCheck") {
    group = "verification"
    description = "Check tower building, waves, combat, losses, reset and NPC network visibility."
    classpath = checks.runtimeClasspath
    mainClass = "wand6.server.TowerDefenceCheck"
    timeout = Duration.ofSeconds(45)
}
tasks.check { dependsOn(towerDefenceCheck) }
