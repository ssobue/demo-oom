package dev.sobue.demo.oom;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Runs the heap exhaustion workload after Spring Boot reports the application as ready. */
@Component
@Slf4j
public class HeapExhaustionTask {

  /** Number of bytes represented by one mebibyte in allocation and reporting calculations. */
  private static final int BYTES_PER_MIB = 1024 * 1024;

  /** Default size of each retained byte array, in mebibytes. */
  private static final int DEFAULT_CHUNK_MIB = 1;

  /** Default interval for progress logs, in mebibytes of retained data. */
  private static final int DEFAULT_REPORT_EVERY_MIB = 16;

  /** Default pause after each allocation, in milliseconds; zero means no pause. */
  private static final int DEFAULT_DELAY_MILLISECONDS = 0;

  /** Parsed command-line arguments used to configure the workload. */
  private final ApplicationArguments applicationArguments;

  /** Virtual thread that owns the allocation loop and can be interrupted during shutdown. */
  private volatile Thread worker;

  /**
   * Creates a task that reads heap exhaustion options from the application arguments.
   *
   * @param applicationArguments parsed command-line arguments
   */
  public HeapExhaustionTask(ApplicationArguments applicationArguments) {
    this.applicationArguments = applicationArguments;
  }

  /** Starts the workload without blocking application startup. */
  // Do not block Spring Boot's startup thread; readiness must be reported before allocation begins.
  @EventListener(ApplicationReadyEvent.class)
  void start() {
    worker = Thread.ofVirtual().name("heap-exhaustion").start(this::run);
  }

  /** Requests the workload thread to stop during graceful application shutdown. */
  @PreDestroy
  void stop() {
    // Interrupt the worker so a graceful shutdown does not leave its delay asleep.
    Thread currentWorker = worker;
    if (currentWorker != null) {
      currentWorker.interrupt();
    }
  }

  /**
   * Runs the configured allocation workload until the heap is exhausted or the worker is
   * interrupted.
   */
  private void run() {
    if (!applicationArguments.containsOption("oom")) {
      log.info("Heap exhaustion is disabled. Start with --oom to exhaust the Java heap.");
      return;
    }

    int chunkMib = positiveIntOption("chunk-mb", DEFAULT_CHUNK_MIB);
    int reportEveryMib = positiveIntOption("report-every-mb", DEFAULT_REPORT_EVERY_MIB);
    int delayMilliseconds = nonNegativeIntOption("delay-ms", DEFAULT_DELAY_MILLISECONDS);
    int chunkBytes = Math.multiplyExact(chunkMib, BYTES_PER_MIB);
    long reportEveryBytes = Math.multiplyExact((long) reportEveryMib, BYTES_PER_MIB);
    // Keep every allocation reachable; otherwise GC would reclaim it instead of exhausting the
    // heap.
    List<byte[]> retained = new ArrayList<>();
    long allocatedBytes = 0;
    long nextReportBytes = reportEveryBytes;

    log.info(
        "Starting heap exhaustion: chunk={} MiB, report interval={} MiB, delay={} ms, max heap={} MiB",
        chunkMib,
        reportEveryMib,
        delayMilliseconds,
        Runtime.getRuntime().maxMemory() / BYTES_PER_MIB);

    while (true) {
      retained.add(new byte[chunkBytes]);
      allocatedBytes += chunkBytes;

      if (allocatedBytes >= nextReportBytes) {
        Runtime runtime = Runtime.getRuntime();
        long usedBytes = runtime.totalMemory() - runtime.freeMemory();
        log.info(
            "Retained approximately {} MiB, used heap={} MiB",
            allocatedBytes / BYTES_PER_MIB,
            usedBytes / BYTES_PER_MIB);
        nextReportBytes = Math.addExact(nextReportBytes, reportEveryBytes);
      }

      if (delayMilliseconds > 0) {
        try {
          Thread.sleep(delayMilliseconds);
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  /**
   * Reads a command-line option that must be greater than zero.
   *
   * @param name option name without the leading {@code --}
   * @param defaultValue value used when the option is absent
   * @return the validated option value
   */
  private int positiveIntOption(String name, int defaultValue) {
    return integerOption(name, defaultValue, 1);
  }

  /**
   * Reads a command-line option that may be zero but cannot be negative.
   *
   * @param name option name without the leading {@code --}
   * @param defaultValue value used when the option is absent
   * @return the validated option value
   */
  private int nonNegativeIntOption(String name, int defaultValue) {
    return integerOption(name, defaultValue, 0);
  }

  /**
   * Parses and validates one integer-valued command-line option.
   *
   * @param name option name without the leading {@code --}
   * @param defaultValue value used when the option is absent
   * @param minimum smallest accepted value
   * @return the validated option value
   * @throws IllegalArgumentException if the option is missing a single integer value or is below
   *     the minimum
   */
  private int integerOption(String name, int defaultValue, int minimum) {
    if (!applicationArguments.containsOption(name)) {
      return defaultValue;
    }

    List<String> values = applicationArguments.getOptionValues(name);
    if (values == null || values.size() != 1) {
      throw new IllegalArgumentException(
          "Option --%s requires exactly one integer value".formatted(name));
    }

    try {
      int value = Integer.parseInt(values.getFirst());
      if (value < minimum) {
        throw new IllegalArgumentException(
            "Option --%s must be at least %d".formatted(name, minimum));
      }
      return value;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException(
          "Option --%s must be an integer".formatted(name), exception);
    }
  }
}
