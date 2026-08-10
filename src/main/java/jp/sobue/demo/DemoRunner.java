package jp.sobue.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DemoRunner implements ApplicationRunner {

  private static final int BYTES_PER_MIB = 1024 * 1024;
  private static final int DEFAULT_CHUNK_MIB = 1;
  private static final int DEFAULT_REPORT_EVERY_MIB = 16;
  private static final int DEFAULT_DELAY_MILLISECONDS = 0;

  @Override
  public void run(ApplicationArguments args) {
    if (!args.containsOption("oom")) {
      System.out.println("OOM demo is disabled. Start with --oom to exhaust the Java heap.");
      return;
    }

    int chunkMib = positiveIntOption(args, "chunk-mb", DEFAULT_CHUNK_MIB);
    int reportEveryMib = positiveIntOption(args, "report-every-mb", DEFAULT_REPORT_EVERY_MIB);
    int delayMilliseconds = nonNegativeIntOption(args, "delay-ms", DEFAULT_DELAY_MILLISECONDS);
    int chunkBytes = Math.multiplyExact(chunkMib, BYTES_PER_MIB);
    long reportEveryBytes = Math.multiplyExact((long) reportEveryMib, BYTES_PER_MIB);
    List<byte[]> retained = new ArrayList<>();
    long allocatedBytes = 0;
    long nextReportBytes = reportEveryBytes;

    System.out.printf(
        "Starting OOM demo: chunk=%d MiB, report interval=%d MiB, delay=%d ms, max heap=%d MiB%n",
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
        System.out.printf(
            "Retained approximately %d MiB, used heap=%d MiB%n",
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

  private int positiveIntOption(ApplicationArguments args, String name, int defaultValue) {
    return integerOption(args, name, defaultValue, 1);
  }

  private int nonNegativeIntOption(ApplicationArguments args, String name, int defaultValue) {
    return integerOption(args, name, defaultValue, 0);
  }

  private int integerOption(ApplicationArguments args, String name, int defaultValue, int minimum) {
    if (!args.containsOption(name)) {
      return defaultValue;
    }

    List<String> values = args.getOptionValues(name);
    if (values == null || values.size() != 1) {
      throw new IllegalArgumentException("Option --%s requires exactly one integer value".formatted(name));
    }

    try {
      int value = Integer.parseInt(values.getFirst());
      if (value < minimum) {
        throw new IllegalArgumentException(
            "Option --%s must be at least %d".formatted(name, minimum));
      }
      return value;
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Option --%s must be an integer".formatted(name), exception);
    }
  }
}
