# SimpleProxy
Very simple TCP reverse proxy

## Usage

First you'll need to generate (SimpleProxy will generate one if config.yml doesn't exist) or create a config.yml like this:
```yaml
epoll: true # optional (default: true): uses epoll on linux if available; no effect on Windows
listeners:
- listenPort: 25565 # port number to listen at
  proxyProtocol: true # optional (default: false): whether the proxy protocol should be enabled for this listener
  servers: # server is selected randomly when a user tries to connect
  - host: 10.0.0.1
    port: 25565
    proxyProtocol: true # optional (default: false): whether the proxy protocol should be enabled for this server
  - host: 10.0.0.2
    port: 25565
    proxyProtocol: false
debug: false
verbose: false
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
# - accept from 1.1.1.1 (rule type of "accept" does not exist)
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
