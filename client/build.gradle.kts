plugins {
    application
}

dependencies {
    implementation(project(":common"))
}

application {
    mainClass = "client.WalkingDesu"
    applicationName = "walking-desu-client"
}

val clientAssets = rootProject.layout.projectDirectory.dir(
    providers.gradleProperty("clientAssetsDir").getOrElse(".devcontainer/assets/client")
)
val requiredAssets = layout.projectDirectory.file("required-assets.txt")

val verifyClientAssets = tasks.register("verifyClientAssets") {
    group = "verification"
    description = "Check that the separately supplied client images are present."
    inputs.file(requiredAssets)
    // File trees allow a useful diagnostic even when the directory is absent.
    inputs.files(fileTree(clientAssets))
    doLast {
        val missing = requiredAssets.asFile.readLines().filter {
            it.isNotBlank() && !clientAssets.file(it).asFile.isFile
        }
        check(missing.isEmpty()) {
            "Missing client assets in ${clientAssets.asFile}:\n" +
                missing.joinToString("\n") +
                "\nSupply -PclientAssetsDir=<directory containing img/>. See README.md."
        }
    }
}

tasks.processResources {
    dependsOn(verifyClientAssets)
    from(clientAssets) {
        include("img/**")
    }
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

tasks.register<Sync>("publishDev") {
    group = "distribution"
    description = "Publish the client, common library and launch scripts for host execution."
    from(tasks.installDist)
    into(rootProject.layout.projectDirectory.dir(".devcontainer/artifacts/client"))
    preserve { include(".gitkeep") }
}

val checks = sourceSets.create("checks") {
    compileClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

val regressionCheck = tasks.register<JavaExec>("regressionCheck") {
    group = "verification"
    description = "Check player death and gold hit bounds with the real client images."
    classpath = checks.runtimeClasspath
    mainClass = "client.ClientRegressionCheck"
    jvmArgs("-Djava.awt.headless=true")
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) }
}
tasks.check { dependsOn(regressionCheck) }
