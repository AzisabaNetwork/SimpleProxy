val log4jVersion = parent!!.extra.get("log4jVersion")

dependencies {
    api(project(":api"))
    api("org.apache.logging.log4j:log4j-core:$log4jVersion")
    api("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")
    api("org.slf4j:slf4j-api:1.8.0-beta4")
}
