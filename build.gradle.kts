plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "net.azisaba"
version = "0.0.1"

repositories {
    mavenCentral()
}

val log4jVersion = "2.17.0"

dependencies {
    implementation("io.netty:netty-all:4.1.70.Final")
    implementation("org.yaml:snakeyaml:1.29")
    implementation("commons-logging:commons-logging:1.2")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("com.github.seancfoley:ipaddress:5.3.3")
    compileOnly("org.jetbrains:annotations:22.0.0")
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
