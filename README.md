# godfather

[![CircleCI](https://travis-ci.org/cndjp/godfather.svg?branch=master)](https://travis-ci.org/cndjp/godfather)

Godfather is a name card creation tool for meetup events.

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
