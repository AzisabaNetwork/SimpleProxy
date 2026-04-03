plugins {
    java
    id("com.gradleup.shadow") version "8.3.10"
    `java-library`
    `maven-publish`
}

group = "net.azisaba.simpleproxy"
version = "2.1.0"

extra.set("log4jVersion", "2.25.4")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

subprojects {
    apply {
        plugin("java")
        plugin("com.gradleup.shadow")
        plugin("java-library")
        plugin("maven-publish")
    }

    group = parent!!.group
    version = parent!!.version

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnlyApi("org.jetbrains:annotations:24.1.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.3")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.3")
    }

    tasks {
        test {
            useJUnitPlatform()
        }

        processResources {
            from(
                sourceSets.main
                    .get()
                    .resources.srcDirs,
            ) {
                include("**")
                val tokenReplacementMap =
                    mapOf(
                        "version" to project.version,
                        "name" to project.parent!!.name,
                    )
                filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
            }
            filteringCharset = "UTF-8"
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(projectDir) { include("LICENSE") }
        }

        jar {
            manifest {
                attributes(
                    "Main-Class" to "net.azisaba.simpleproxy.proxy.Main",
                    "Multi-Release" to true,
                )
            }
        }
    }
}

allprojects {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }

        withJavadocJar()
        withSourcesJar()
    }

    val javaComponent = components["java"] as AdhocComponentWithVariants
    javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
        skip()
    }

    publishing {
        repositories {
            maven {
                name = "repo"
                credentials(PasswordCredentials::class)
                url =
                    uri(
                        if (project.version.toString().endsWith("SNAPSHOT")) {
                            project.findProperty("deploySnapshotURL")
                                ?: System.getProperty("deploySnapshotURL", "https://repo.azisaba.net/repository/maven-snapshots/")
                        } else {
                            project.findProperty("deployReleasesURL")
                                ?: System.getProperty("deployReleasesURL", "https://repo.azisaba.net/repository/maven-releases/")
                        },
                    )
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks.getByName("sourcesJar"))
            }
        }
    }
}
