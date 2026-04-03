dependencies {
    api("org.yaml:snakeyaml:2.6")
    api("commons-logging:commons-logging:1.3.0")
    api("io.netty:netty-all:4.2.12.Final")
    api("org.apache.logging.log4j:log4j-api:${parent!!.extra.get("log4jVersion")}")
    api("com.github.seancfoley:ipaddress:5.6.2")
}
