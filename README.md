# demo-oom

A small Spring Boot command-line application that intentionally exhausts the Java heap and raises `java.lang.OutOfMemoryError`.

## Warning

This application is designed to consume all available heap memory.

Run it only in a disposable environment with a deliberately small heap limit.

Do not run the OOM mode in production, on a shared machine, or against a process that owns important data.

Stop the process with `Ctrl-C` if you no longer need the demonstration.

## Requirements

- JDK 25 or newer
- Gradle Wrapper included in this repository
- Docker Compose v2 for the monitoring example

## Build

```sh
./gradlew clean build
```

The build does not start OOM mode, so the test suite can run normally.

## Run the demonstration

Build the executable JAR, then start it with a small maximum heap:

```sh
java -Xmx64m -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar --oom
```

The application retains one mebibyte-sized byte array per iteration.

Because every array remains reachable from a list, garbage collection cannot reclaim the allocated arrays.

The process eventually terminates with an error similar to this:

```text
java.lang.OutOfMemoryError: Java heap space
```

The exact amount retained before the error depends on the JDK, JVM options, and other runtime allocations.

## Options

| Option | Default | Description |
| --- | ---: | --- |
| `--oom` | disabled | Enables the intentional heap exhaustion loop. |
| `--chunk-mb` | `1` | Size of each retained byte array in MiB. |
| `--report-every-mb` | `16` | Progress reporting interval in MiB. |
| `--delay-ms` | `0` | Delay after each allocation in milliseconds. |

For example, this command uses 4 MiB chunks and reports every 32 MiB:

```sh
java -Xmx128m -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar \
  --oom --chunk-mb=4 --report-every-mb=32 --delay-ms=100
```

Without `--oom`, the application starts its HTTP server, prints a message, and waits without allocating the demonstration data:

```sh
java -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar
```

## Monitor with Docker Compose

Docker Compose starts the OOM demo and a Prometheus server on the same network.

The Compose configuration limits the application heap to 256 MiB and inserts a one second delay between allocations.

This gives Prometheus several minutes of samples while the heap rises before the application reaches `OutOfMemoryError`.

Start both services with:

```sh
docker compose up --build -d
```

Follow the application logs in a separate terminal:

```sh
docker compose logs -f app
```

Open these endpoints while the demo is running:

| URL | Purpose |
| --- | --- |
| http://localhost:8080 | Spring Boot application port |
| http://localhost:8081/actuator/prometheus | Prometheus text endpoint |
| http://localhost:9090 | Prometheus web UI |

In the Prometheus UI, try these queries:

```promql
jvm_memory_used_bytes{area="heap"}
jvm_memory_committed_bytes{area="heap"}
jvm_memory_max_bytes{area="heap"}
```

The Prometheus target is `app:8081`, which is the Compose service name and the management port inside the Compose network.

The application eventually stops with `OutOfMemoryError`, but Prometheus remains available so the collected samples can be inspected.

Stop the services with:

```sh
docker compose down
```

See [README-ja.md](README-ja.md) for the Japanese documentation.
