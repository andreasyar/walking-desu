plugins {
    application
}

dependencies {
    implementation(project(":common"))
}

application {
    mainClass = "wand6.client.Wand"
    applicationName = "wand6-client"
}

tasks.jar {
    manifest.attributes["Implementation-Version"] = project.version.toString()
}

val clientAssets = rootProject.layout.projectDirectory.dir(
    providers.gradleProperty("clientAssetsDir").getOrElse(".devcontainer/assets/wand6")
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
    // Images are local runtime inputs, never contents of a published JAR.
    exclude("img/**")
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

tasks.startScripts {
    doLast {
        val unixClasspath = Regex("(?m)^CLASSPATH=[^\\r\\n]*")
        val windowsClasspath = Regex("(?m)^set CLASSPATH=[^\\r\\n]*")
        check(unixClasspath.findAll(unixScript.readText()).count() == 1)
        check(windowsClasspath.findAll(windowsScript.readText()).count() == 1)
        unixScript.writeText(unixClasspath.replace(unixScript.readText()) {
            it.value + ":\$APP_HOME/assets"
        })
        windowsScript.writeText(windowsClasspath.replace(windowsScript.readText()) {
            it.value + ";%APP_HOME%\\assets"
        })
    }
}

distributions.main {
    contents {
        from(requiredAssets) { into("assets") }
        from(rootProject.file("docs/CLIENT-ASSETS.md"))
    }
}

val verifyClientPackaging = tasks.register("verifyClientPackaging") {
    group = "verification"
    description = "Reject images in public client JARs and distributions."
    dependsOn(tasks.jar, tasks.distZip, tasks.distTar)
    doLast {
        val jars = listOf(tasks.jar.get().archiveFile.get().asFile) +
            configurations.runtimeClasspath.get().files.filter { it.extension == "jar" }
        val trees = jars.map { zipTree(it) } + listOf(
            zipTree(tasks.distZip.get().archiveFile.get()),
            tarTree(tasks.distTar.get().archiveFile.get())
        )
        trees.forEach { tree ->
            val images = tree.matching { include("**/img/**") }.files +
                tree.files.filter {
                    it.extension.lowercase() in setOf("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg", "ico")
                }
            check(images.isEmpty()) { "Images must remain local, outside public archives: $images" }
        }
    }
}
tasks.check { dependsOn(verifyClientPackaging) }

tasks.register<Sync>("publishDev") {
    group = "distribution"
    description = "Prepare local client scripts, libraries and separately supplied images."
    dependsOn(verifyClientAssets)
    from(tasks.installDist)
    from(clientAssets) {
        into("assets")
        include("img/**")
        exclude("img/peasant.png", "img/peon.png")
    }
    into(rootProject.layout.projectDirectory.dir(".devcontainer/artifacts/wand6-client"))
    preserve { include(".gitkeep") }
}

val checks = sourceSets.create("checks") {
    compileClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

tasks.register<Jar>("guiCheckAgent") {
    archiveClassifier = "gui-check-agent"
    from(checks.output)
    manifest.attributes["Premain-Class"] = "wand6.client.GraphicalCheckAgent"
    manifest.attributes["Implementation-Version"] = project.version.toString()
}

tasks.named<JavaExec>("run") {
    dependsOn(verifyClientAssets)
    classpath += files(clientAssets)
}

// Local asset-dependent verification; public packaging remains asset-free.
tasks.register<JavaExec>("visualCheck") {
    group = "verification"
    dependsOn(verifyClientAssets)
    classpath = checks.runtimeClasspath + files(clientAssets)
    mainClass = "wand6.client.VisualCheck"
    jvmArgs("-Djava.awt.headless=true")
}

tasks.register<Jar>("multiplayerGuiCheckAgent") {
    archiveClassifier = "multiplayer-gui-check-agent"
    from(checks.output)
    manifest.attributes["Premain-Class"] = "wand6.client.MultiplayerGraphicalAgent"
    manifest.attributes["Implementation-Version"] = project.version.toString()
}


tasks.register<Jar>("combatGuiCheckAgent") {
    archiveClassifier = "combat-gui-check-agent"
    from(checks.output)
    manifest.attributes["Premain-Class"] = "wand6.client.CombatGraphicalAgent"
    manifest.attributes["Implementation-Version"] = project.version.toString()
}

tasks.register<Jar>("towerDefenceGuiCheckAgent") {
    archiveClassifier = "tower-defence-gui-check-agent"
    from(checks.output)
    manifest.attributes["Premain-Class"] = "wand6.client.TowerDefenceGraphicalAgent"
    manifest.attributes["Implementation-Version"] = project.version.toString()
}
