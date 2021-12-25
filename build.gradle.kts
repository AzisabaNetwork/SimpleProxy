plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.1"
    `java-library`
}

group = "net.azisaba.simpleProxy"
version = "0.0.1"

extra.set("log4jVersion", "2.17.0")

subprojects {
    apply {
        plugin("java")
        plugin("com.github.johnrengelman.shadow")
        plugin("java-library")
    }

    group = parent!!.group
    version = parent!!.version

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:23.0.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    }

    tasks {
        getByName<Test>("test") {
            useJUnitPlatform()
        }

        withType<ProcessResources> {
            from(sourceSets.main.get().resources.srcDirs) {
                include("**")
                val tokenReplacementMap = mapOf(
                    "version" to project.version,
                    "name" to project.name,
                )
                filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
            }
            filteringCharset = "UTF-8"
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(projectDir) { include("LICENSE") }
        }

        withType<Jar> {
            manifest {
                attributes(
                    "Main-Class" to "net.azisaba.simpleProxy.Main",
                    "Multi-Release" to true,
                )
            }
        }
    }
}
