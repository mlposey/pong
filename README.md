# Pong
This is a multiplayer implementation of the classic pong game. The client is written in Javascript and the server in Java. WebSocket
provides the communication protocol for message exchange.

A live version of the game is located at pong.marcusposey.com.

# Deployment
## Manual Setup
The preferred process for a manual setup requires the JDK and Maven.
1. Enter the `server/` directory of this project root.
2. Run `mvn install`.
3. Start the server with `java -jar target/pong-server-1.0-SNAPSHOT-jar-with-dependencies.jar`.
4. Direct a web browser toward `client/index.html`.

## Docker Compose
[Docker Compose](https://docs.docker.com/compose/overview/) offers a simpler setup process than above.
1. Run `docker-compose -f docker-compose.local.yaml up --build`
2. Direct a web browser toward "localhost".