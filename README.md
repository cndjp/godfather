# godfather

[![CircleCI](https://travis-ci.org/cndjp/godfather.svg?branch=master)](https://travis-ci.org/cndjp/godfather)

Godfather is a name card creation tool for meetup events.

# Create Event Card

1. Run

```bash
$ sbt assembly
$ java -jar target/scala-2.12/godfather.jar -event-url <Connpass Event URL>
15:13:50.595 [main] INFO com.twitter.util.logging.Slf4jBridgeUtility$ - org.slf4j.bridge.SLF4JBridgeHandler installed.
...
15:13:51.833 [main] INFO com.github.cndjp.godfather.Godfather$ - Scrape URL: https://cnd.connpass.com/event/154414/
15:13:52.168 [main] INFO com.github.cndjp.godfather.Godfather$ - Godfather Ready!! ☕️
```

2. Do Get Request This Server from Web Browser

```
GET http://localhost:8080/render
```

Please wait for a while...

3.　Redirect!!

# Build

### Jar

```bash
sbt assembly
ls target/scala-2.12/godfather.jar                                      
```

### Docker

```bash
docker build -t cndjp/godfather:latest -build-arg <Connpass Event URL> .
```

# Run

### Jar

```bash
java -jar target/scala-2.12/godfather.jar -event-url <Connpass Event URL>
```

### Docker

```bash
docker run -p 8080:8080 -it --rm cndjp/godfather:latest 
```

# Test

```bash
sbt test
```
