plugins {
    base
}

subprojects {
    group = "walkingdesu"
    version = "4.2.0-dev"

    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion = JavaLanguageVersion.of(21)
        }
        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release = 21
            options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        }
    }
    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}

tasks.assemble {
    dependsOn(":common:assemble", ":client:assemble", ":server:assemble")
}
tasks.check {
    dependsOn(":common:check", ":client:check", ":server:check")
}
tasks.clean {
    dependsOn(":common:clean", ":client:clean", ":server:clean")
}

tasks.wrapper {
    gradleVersion = "9.8.2"
    distributionType = Wrapper.DistributionType.BIN
    distributionSha256Sum = "21bd5da2de49b642904077f38c0efdc7778e92b3ed0889392dca61b97e389607"
}
