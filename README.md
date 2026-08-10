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

For example, this command uses 4 MiB chunks and reports every 32 MiB:

```sh
java -Xmx128m -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar \
  --oom --chunk-mb=4 --report-every-mb=32
```

Without `--oom`, the application prints a message and exits without allocating the demonstration data:

```sh
java -jar build/libs/demo-oom-0.0.1-SNAPSHOT.jar
```

See [README-ja.md](README-ja.md) for the Japanese documentation.
