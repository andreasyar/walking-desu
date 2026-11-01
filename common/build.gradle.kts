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
