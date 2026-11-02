plugins {
    `java-library`
}

val checks = sourceSets.create("checks") {
    compileClasspath += sourceSets.main.get().runtimeClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}
val movementCheck = tasks.register<JavaExec>("movementCheck") {
    group = "verification"
    classpath = checks.runtimeClasspath
    mainClass = "wand6.common.MovementCheck"
}
tasks.check { dependsOn(movementCheck) }

val serverTimeCheck = tasks.register<JavaExec>("serverTimeCheck") {
    group = "verification"
    classpath = checks.runtimeClasspath
    mainClass = "wand6.common.ServerTimeCheck"
}
tasks.check { dependsOn(serverTimeCheck) }

val inventoryCheck = tasks.register<JavaExec>("inventoryCheck") {
    group = "verification"
    classpath = checks.runtimeClasspath
    mainClass = "wand6.common.InventoryCheck"
}
tasks.check { dependsOn(inventoryCheck) }
