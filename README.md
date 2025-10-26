# MCServerStatus
It's a basic implementation of Minecraft: Java Edition Server List ping protocol

# Usage
The project comes with mainly a builer class: "ServerStatus", which has all the implementation & the methods you can use

## Methods
Everything is summed up in this example

```java
ServerStatus status = new ServerStatus.Builder()
.hostname("serverIp") // this is the server's hostname/ip that you want to ping(default is "localhost")
.port(25566) //the desired server's port (default is 25565)
.ping(true) //if you want to also ping(latency check) the server(default is true)
.debug(true) //if you want to see the incoming & outgoing packets(default is false)
.build()

String response = status.getStatus(); //returns a JSON formatted String (returns Null if failed to fetch status)
long piung = status.getPing() //return the ping(return -1 if .ping(false))

```
# Contribution/Ideas
if you want to improve upon this code or want to contribute by sharing ideas, feel free to open issues and i'll happily check
