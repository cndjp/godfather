# godfather

[![CircleCI](https://travis-ci.org/cndjp/godfather.svg?branch=master)](https://travis-ci.org/cndjp/godfather)

Godfather is a name card creation tool for meetup events.

# Create Event Card

1. Run

```bash
$ sbt assembly
$ java -jar target/scala-2.12/godfather.jar --event-url <Connpass Event URL>
15:13:50.595 [main] INFO com.twitter.util.logging.Slf4jBridgeUtility$ - org.slf4j.bridge.SLF4JBridgeHandler installed.
...
15:13:51.833 [main] INFO com.github.cndjp.godfather.Godfather$ - Scrape URL: <Connpass Event URL>
15:56:19.989 [main] INFO com.github.cndjp.godfather.usecase.render.RenderUsecaseImpl - Collect Participants: [ORGANIZER]
15:56:20.455 [main] INFO com.github.cndjp.godfather.infrastructure.repository.participant.ConnpassParticipantRepositoryImpl - nnao45: 1 / 14
15:56:21.015 [main] INFO com.github.cndjp.godfather.infrastructure.repository.participant.ConnpassParticipantRepositoryImpl - nnao45: 2 / 14
...
15:57:57.060 [main] INFO com.github.cndjp.godfather.usecase.render.RenderUsecaseImpl - Finish for rendering!!⭐️
15:57:57.168 [main] INFO com.github.cndjp.godfather.Godfather$ - Godfather Ready!! ☕️
15:57:57.168 [main] INFO com.github.cndjp.godfather.Godfather$ - Please Check it 👉 http://localhost:8080/index.html
```

2. Do Get Request This Server from Web Browser

```
GET http://localhost:8080/index.html
```

![demo](https://raw.githubusercontent.com/cndjp/godfather/master/src/main/resources/demo01.png)

# Build

### Jar

```bash
sbt assembly
```

### Docker

```bash
docker build -t cndjp/godfather:latest --build-arg <Connpass Event URL> .
```

# Run

### Jar

```bash
java -jar target/scala-2.12/godfather.jar --event-url <Connpass Event URL>
```

### Docker

```bash
docker run -p 8080:8080 -it --rm cndjp/godfather:latest 
```

# Test

```bash
sbt test
```
