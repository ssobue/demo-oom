package dev.sobue.demo.oom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the Spring Boot application used to observe controlled heap exhaustion. */
@SpringBootApplication
public class HeapExhaustionApplication {

  /** Creates the application configuration object. */
  public HeapExhaustionApplication() {}

  /**
   * Launches the application and its Actuator endpoints.
   *
   * @param args command-line arguments passed to the application
   */
  public static void main(String[] args) {
    SpringApplication.run(HeapExhaustionApplication.class, args);
  }
}
