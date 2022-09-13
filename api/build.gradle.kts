dependencies {
    api("org.yaml:snakeyaml:1.30")
    api("commons-logging:commons-logging:1.2")
    api("io.netty:netty-all:4.1.82.Final")
    api("org.apache.logging.log4j:log4j-api:${parent!!.extra.get("log4jVersion")}")
    api("com.github.seancfoley:ipaddress:5.3.4")
}
