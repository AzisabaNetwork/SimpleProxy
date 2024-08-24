dependencies {
    api("org.yaml:snakeyaml:2.2")
    api("commons-logging:commons-logging:1.3.0")
    api("io.netty:netty-all:4.1.107.Final")
    api("org.apache.logging.log4j:log4j-api:${parent!!.extra.get("log4jVersion")}")
    api("com.github.seancfoley:ipaddress:5.5.1")
}
