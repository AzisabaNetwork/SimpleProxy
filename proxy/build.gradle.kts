val log4jVersion = parent!!.extra.get("log4jVersion")

dependencies {
    api(project(":api"))
    api("org.apache.logging.log4j:log4j-core:$log4jVersion")
    api("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    api("org.slf4j:slf4j-api:2.0.3")
    api("it.unimi.dsi:fastutil:8.5.9")
}

tasks {
    getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveFileName.set("SimpleProxy.jar")
    }
}
