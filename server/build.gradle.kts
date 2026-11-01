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
    mainClass = "server.javatestserver.JavaTestServer"
    applicationName = "walking-desu-server"
}

val executableJar = tasks.register<Jar>("executableJar") {
    group = "build"
    description = "Build a standalone server JAR, including its runtime dependencies."
    archiveBaseName = "walking-desu-server"
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

tasks.register("publishDev") {
    group = "distribution"
    description = "Atomically publish server.jar; stop the running server before this task."
    dependsOn(executableJar)
    val source = executableJar.flatMap { it.archiveFile }
    val destination = rootProject.layout.projectDirectory.file(".devcontainer/artifacts/server/server.jar")
    inputs.file(source)
    outputs.file(destination)
    doLast {
        val target = destination.asFile.toPath()
        Files.createDirectories(target.parent)
        val staging = Files.createTempFile(target.parent, "server-", ".jar.tmp")
        try {
            Files.copy(source.get().asFile.toPath(), staging, REPLACE_EXISTING)
            Files.move(staging, target, ATOMIC_MOVE, REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(staging)
        }
    }
}

val checks = sourceSets.create("checks") {
    compileClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

val networkSmoke = tasks.register<JavaExec>("networkSmoke") {
    group = "verification"
    description = "Run protocol checks against an isolated server on an OS-assigned port."
    classpath = checks.runtimeClasspath
    mainClass = "server.javatestserver.NetworkSmokeCheck"
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) }
    timeout = Duration.ofSeconds(90)
}
tasks.check { dependsOn(networkSmoke) }
