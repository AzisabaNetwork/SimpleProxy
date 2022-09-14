dependencies {
    api("org.yaml:snakeyaml:1.32")
    api("commons-logging:commons-logging:1.2")
    api("io.netty:netty-all:4.1.79.Final")
    api("org.apache.logging.log4j:log4j-api:${parent!!.extra.get("log4jVersion")}")
    api("com.github.seancfoley:ipaddress:5.3.4")
}
