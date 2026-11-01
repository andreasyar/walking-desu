plugins {
    `java-library`
}

val checks = sourceSets.create("checks") {
    compileClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}
val inventoryCheck = tasks.register<JavaExec>("inventoryCheck") {
    group = "verification"
    description = "Check inventory conservation, rejected mutations and item message serialization."
    classpath = checks.runtimeClasspath
    mainClass = "common.InventoryCheck"
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) }
}
tasks.check { dependsOn(inventoryCheck) }
