# SimpleProxy
Very simple TCP reverse proxy

## Usage

First you'll need to generate (SimpleProxy will generate one if config.yml doesn't exist) or create a config.yml.

Example config.yml:
```yaml
epoll: true # optional (default: true): uses epoll on linux if available; no effect on Windows
listeners:
- listenPort: 25565 # port number to listen at
  host: 0.0.0.0 # default: 0.0.0.0
  proxyProtocol: true # optional (default: false): whether the proxy protocol should be enabled for this listener
  timeout: 3000 # custom timeout duration in milliseconds (default: 30000 = 30 seconds)
  # ^ don't use 3 seconds as timeout in production because it is (probably) too short
  servers: # server is selected randomly when a user tries to connect
  - host: 10.0.0.1
    port: 25565
    proxyProtocol: true # optional (default: false): whether the proxy protocol should be enabled for this server
  - host: 10.0.0.2
    port: 25565
    proxyProtocol: false
disablePlugins: false # whether to prevent loading plugins (default: false)
debug: false # enable only if you want more verbose logging. this may or may not affect performance (default: false)
verbose: false # if enabled, rule check handler will log a message when denying connection (default: true)
# Rule type must be one of these: deny, allow
#   deny - disconnects immediately after the connection
#   allow - accepts the connection
# Example of acceptable syntax for rules:
# - deny connections from 1.1.1.1/32
# - deny from ip 1.1.1.1/32
# - deny 1.1.1.1
# - allow 8.8.8.8
# - deny 10.0.0.0/24
# - deny connection from ip 0.0.0.0/0
# - allow ::1
# Example of unacceptable (invalid) syntax:
# - drop from 1.1.1.1 (rule type of "drop" isn't supported)
# - accept from 1.1.1.1 (rule type of "accept" isn't supported)
# - pls accept from all (first token must be rule type)
# - deny from all (please specify 0.0.0.0/0 (v4) and/or ::/0 (v6) manually)
# Defaults to ALLOW if ip address is not in any of defined rules
rules:
  - allow from 192.168.0.0/24
  - allow from 127.0.0.1
  - allow from ::1
  - deny from ::/0
  - deny from 0.0.0.0/0
```
